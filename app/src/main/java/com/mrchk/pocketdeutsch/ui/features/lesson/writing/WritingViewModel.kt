package com.mrchk.pocketdeutsch.ui.features.lesson.writing

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.data.local.WrittenTaskResultEntity
import com.mrchk.pocketdeutsch.domain.model.ProficiencyLevel
import com.mrchk.pocketdeutsch.domain.model.TaskRequirement
import com.mrchk.pocketdeutsch.domain.model.TextCorrection
import com.mrchk.pocketdeutsch.domain.model.WritingTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.mrchk.pocketdeutsch.data.repository.GeminiRepository
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WritingViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _uiState = MutableStateFlow(WritingUiState(isLoading = true))
    val uiState: StateFlow<WritingUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<WrittenTaskResultEntity>>(emptyList())
    val history: StateFlow<List<WrittenTaskResultEntity>> = _history.asStateFlow()

    init {
        loadWritingTask()
    }

    private fun loadWritingTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val lesson = lessonRepository.getLessonById(lessonId)
                val jsonTask = lesson?.writingExercise // Твій об'єкт із JSON

                if (lesson != null && jsonTask != null) {

                    // 1. Розбиваємо instruction на рядки
                    val instructionLines = jsonTask.instruction.lines()

                    // 2. Витягуємо чекліст: шукаємо всі рядки, що починаються з тире (звичайного '-' або довгого '–')
                    val bulletPoints = instructionLines
                        .filter { it.trim().startsWith("-") || it.trim().startsWith("–") }
                        .map { it.trim().removePrefix("-").removePrefix("–").trim() }

                    // 3. Формуємо основний текст завдання: беремо все, КРІМ буліт-поінтів
                    val mainPromptText = instructionLines
                        .filterNot { it.trim().startsWith("-") || it.trim().startsWith("–") }
                        .joinToString("\n")
                        .trim()

                    // 4. Створюємо фінальну модель
                    val uiTask = WritingTask(
                        id = lessonId,
                        // Якщо lesson.level є, беремо його. Якщо ні - парсимо з формату (напр., "telc_A2_Schreiben")
                        level = ProficiencyLevel.valueOf(lesson.level.uppercase()),

                        // Перетворюємо "Mitteilung_oder_Nachricht" на красиве "Mitteilung oder Nachricht"
                        title = jsonTask.type.replace("_", " "),

                        promptText = mainPromptText,

                        // Витягуємо ліміт слів (можна жорстко задати 40 для A2, або динамічно)
                        minWords = if (lesson.level.contains("B1", ignoreCase = true)) 80 else 40,

                        // Мапимо наші витягнуті рядки у TaskRequirement
                        requiredPoints = bulletPoints.mapIndexed { index, text ->
                            TaskRequirement(
                                id = "${lessonId}_req_$index",
                                text = text,
                                isChecked = false
                            )
                        },
                        hints = emptyList() // В JSON їх немає, залишаємо порожнім
                    )

                    // 5. Оновлюємо State
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            task = uiTask,
                            checklist = uiTask.requiredPoints, // Чекліст на екрані автоматично оживе!
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Завдання не знайдено")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Помилка завантаження: ${e.message}")
                }
            }
        }
    }

    fun onTextChanged(newText: String) {
        _uiState.update {
            it.copy(
                textInput = newText,
                errorMessage = null,
            )
        }
    }

    fun onChecklistItemToggled(itemId: String, isChecked: Boolean) {
        _uiState.update { currentState ->
            val updatedChecklist = currentState.checklist.map { item ->
                if (item.id == itemId) item.copy(isChecked = isChecked) else item
            }
            currentState.copy(checklist = updatedChecklist)
        }
    }

    fun onRedemittelClicked(phrase: String) {
        _uiState.update { it.copy(textInput = it.textInput + phrase) }
    }

    fun submitForEvaluation() {
        val currentState = _uiState.value
        val studentText = currentState.textInput
        val task = currentState.task ?: return

        if (studentText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Текст не може бути порожнім") }
            return
        }

        if (!studentText.any { it.isLetter() }) {
            _uiState.update { it.copy(errorMessage = "Текст повинен містити слова") }
            return
        }

        if (currentState.wordCount < 3) {
            _uiState.update { it.copy(errorMessage = "Напиши хоча б 3 слова") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, isLoading = true) }

            try {
                val evaluationResult = withContext(Dispatchers.IO) {
                    geminiRepository.evaluateText(task, studentText)
                }

                geminiRepository.saveResult(
                    exerciseId = task.id,
                    originalText = studentText,
                    evaluation = evaluationResult
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        result = evaluationResult
                    )
                }

            } catch (e: Exception) {
                Log.e("WritingViewModel", "Помилка ШІ: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetEvaluation() {
        _uiState.update { it.copy(result = null) }
    }

    fun onCorrectionSelected(correction: TextCorrection) {
        _uiState.update { it.copy(selectedCorrection = correction) }
    }

    fun clearSelectedCorrection() {
        _uiState.update { it.copy(selectedCorrection = null) }
    }

    fun loadHistory(exerciseId: String) {
        viewModelScope.launch {
            geminiRepository.getHistoryForExercise(exerciseId).collect { savedResults ->
                _history.value = savedResults
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun loadMockTask() {
        val mockTask = WritingTask(
            id = "task_1",
            level = ProficiencyLevel.B1,
            title = "Schreiben: E-Mail",
            promptText = "Deine Freundin Anna hat dich zur Geburtstagsparty eingeladen...",
            minWords = 40,
            requiredPoints = listOf(
                TaskRequirement("1", "welche Ausflüge Sie mit Marianne machen wollen"),
                TaskRequirement("2", "welche Kleidung sie mitnehmen soll")
            ),
            hints = listOf("Hallo Anna,\n\n", "Vielen Dank für ", "\n\nLiebe Grüße,\n")
        )

        _uiState.update {
            it.copy(
                task = mockTask,
                checklist = mockTask.requiredPoints
            )
        }
    }
}
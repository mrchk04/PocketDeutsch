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
            _uiState.update { it.copy(isLoading = true) }
            try {
                val lesson = lessonRepository.getLessonById(lessonId)
                val jsonTask = lesson?.examPractice?.writing

                if (lesson != null && jsonTask != null) {
                    val promptText = jsonTask.instruction.firstOrNull() ?: ""
                    val bulletPoints = jsonTask.instruction.drop(1)

                    val uiTask = WritingTask(
                        id = lessonId,
                        level = ProficiencyLevel.valueOf(lesson.level.take(2).uppercase()),
                        title = jsonTask.format.replace("_", " ").replaceFirstChar { it.uppercase() },
                        promptText = promptText,
                        minWords = if (lesson.level.contains("B1")) 80 else 40,
                        requiredPoints = bulletPoints.mapIndexed { index, text ->
                            TaskRequirement("${lessonId}_$index", text, false)
                        },
                        hints = emptyList()
                    )

                    _uiState.update { it.copy(isLoading = false, task = uiTask, checklist = uiTask.requiredPoints) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Помилка завантаження: ${e.message}")
                }
            }
        }
    }

    private suspend fun completeWritingNode() {
        val currentLessonId = lessonId
        try {
            val nodeId = "${currentLessonId}_writing"
            lessonRepository.completeNode(nodeId)
            Log.d("WritingViewModel", "Progress updated for node: $nodeId")
        } catch (e: Exception) {
            Log.e("WritingViewModel", "Помилка при збереженні прогресу: ${e.message}")
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

                completeWritingNode()

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
}
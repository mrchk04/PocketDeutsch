package com.mrchk.pocketdeutsch.ui.features.writing

import android.util.Log
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
import kotlinx.serialization.json.Json
import com.mrchk.pocketdeutsch.data.repository.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WritingViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WritingUiState())
    val state = _state.asStateFlow()

    private val _history = MutableStateFlow<List<WrittenTaskResultEntity>>(emptyList())
    val history: StateFlow<List<WrittenTaskResultEntity>> = _history.asStateFlow()

    init {
        loadMockTask()
    }

    fun onTextChanged(newText: String) {
        _state.update {
            it.copy(
                textInput = newText,
                errorMessage = null,
            )
        }
    }

    fun onChecklistItemToggled(itemId: String, isChecked: Boolean) {
        _state.update { currentState ->
            val updatedChecklist = currentState.checklist.map { item ->
                if (item.id == itemId) item.copy(isChecked = isChecked) else item
            }
            currentState.copy(checklist = updatedChecklist)
        }
    }

    fun onRedemittelClicked(phrase: String) {
        _state.update { it.copy(textInput = it.textInput + phrase) }
    }

    fun submitForEvaluation() {
        val currentState = _state.value
        val studentText = currentState.textInput
        val task = currentState.task ?: return

        if (studentText.isBlank()) {
            _state.update { it.copy(errorMessage = "Текст не може бути порожнім") }
            return
        }

        if (!studentText.any { it.isLetter() }) {
            _state.update { it.copy(errorMessage = "Текст повинен містити слова") }
            return
        }

        if (currentState.wordCount < 3) {
            _state.update { it.copy(errorMessage = "Напиши хоча б 3 слова") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(errorMessage = null, isLoading = true) }

            try {
                val evaluationResult = withContext(Dispatchers.IO) {
                    geminiRepository.evaluateText(task, studentText)
                }

                geminiRepository.saveResult(
                    exerciseId = task.id,
                    originalText = studentText,
                    evaluation = evaluationResult
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        result = evaluationResult
                    )
                }

            } catch (e: Exception) {
                Log.e("WritingViewModel", "Помилка ШІ: ${e.message}", e)
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetEvaluation() {
        _state.update { it.copy(result = null) }
    }

    fun onCorrectionSelected(correction: TextCorrection) {
        _state.update { it.copy(selectedCorrection = correction) }
    }

    fun clearSelectedCorrection() {
        _state.update { it.copy(selectedCorrection = null) }
    }

    fun loadHistory(exerciseId: String) {
        viewModelScope.launch {
            geminiRepository.getHistoryForExercise(exerciseId).collect { savedResults ->
                _history.value = savedResults
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
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

        _state.update {
            it.copy(
                task = mockTask,
                checklist = mockTask.requiredPoints
            )
        }
    }
}
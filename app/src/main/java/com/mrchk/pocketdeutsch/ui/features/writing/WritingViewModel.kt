package com.mrchk.pocketdeutsch.ui.features.writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.AiEvaluationResult
import com.mrchk.pocketdeutsch.domain.model.ChecklistEvaluation
import com.mrchk.pocketdeutsch.domain.model.ProficiencyLevel
import com.mrchk.pocketdeutsch.domain.model.TaskRequirement
import com.mrchk.pocketdeutsch.domain.model.TextCorrection
import com.mrchk.pocketdeutsch.domain.model.WritingTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WritingViewModel : ViewModel() {

    private val _state = MutableStateFlow(WritingUiState())
    val state = _state.asStateFlow()

    init {
        loadMockTask()
    }

    fun onTextChanged(newText: String){
        _state.update { it.copy(textInput = newText) }
    }

    fun onChecklistItemToggled(itemId: String, isChecked: Boolean){
        _state.update{ currentState ->
            val updatedChecklist = currentState.checklist.map {item ->
                if (item.id == itemId) item.copy(isChecked = isChecked) else item
            }
            currentState.copy(checklist = updatedChecklist)
        }
    }

    fun onRedemittelClicked(phrase: String){
        _state.update { it.copy(textInput = it.textInput + phrase) }
    }

    fun submitForEvaluation(){
        if(_state.value.textInput.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            delay(3000)

            val mockResult = AiEvaluationResult(
                score = 85,
                overallFeedback = "Super gemacht! Ти добре впоралася із завданням, але зверни увагу на порядок слів у підрядних реченнях.",
                checklistEvaluations = listOf(
                    ChecklistEvaluation("1", true, "Чудово описано ідеї для екскурсій."),
                    ChecklistEvaluation("2", false, "Ти забула згадати про одяг.")
                ),
                textCorrections = emptyList()
            )

            _state.update {
                it.copy(
                    isLoading = false,
                    result = mockResult
                )
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
package com.mrchk.pocketdeutsch.ui.features.writing

import com.mrchk.pocketdeutsch.domain.model.AiEvaluationResult
import com.mrchk.pocketdeutsch.domain.model.TaskRequirement
import com.mrchk.pocketdeutsch.domain.model.TextCorrection
import com.mrchk.pocketdeutsch.domain.model.WritingTask

data class WritingUiState(
    val task: WritingTask? = null,
    val checklist: List<TaskRequirement> = emptyList(),
    val textInput: String = "",
    val isLoading: Boolean = false,
    val result: AiEvaluationResult? = null,
    val selectedCorrection: TextCorrection? = null
){
    val wordCount: Int
        get() = if (textInput.isBlank()) 0 else textInput.trim().split("\\s+".toRegex()).size
}
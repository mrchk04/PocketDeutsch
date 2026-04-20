package com.mrchk.pocketdeutsch.ui.features.lesson.writing

import com.mrchk.pocketdeutsch.data.local.WrittenTaskResultEntity

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    object Empty : HistoryUiState
    data class Success(val tasks: List<WrittenTaskResultEntity>) : HistoryUiState
}
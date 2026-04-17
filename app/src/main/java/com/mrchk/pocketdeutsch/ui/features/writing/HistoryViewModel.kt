package com.mrchk.pocketdeutsch.ui.features.writing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.data.local.WrittenTaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val writtenTaskDao: WrittenTaskDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Ключ має збігатися з тим, що ти передаєш у NavHost
    private val exerciseId: String = checkNotNull(savedStateHandle["exerciseId"])

    val uiState: StateFlow<HistoryUiState> = writtenTaskDao.getResultsForExercise(exerciseId)
        .map { tasks ->
            if (tasks.isEmpty()) HistoryUiState.Empty else HistoryUiState.Success(tasks)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HistoryUiState.Loading
        )
}
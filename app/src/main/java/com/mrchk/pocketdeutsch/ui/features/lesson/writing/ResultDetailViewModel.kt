package com.mrchk.pocketdeutsch.ui.features.lesson.writing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.data.local.WrittenTaskDao
import com.mrchk.pocketdeutsch.data.local.WrittenTaskResultEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultDetailViewModel @Inject constructor(
    private val writtenTaskDao: WrittenTaskDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val timestamp: Long = checkNotNull(savedStateHandle["timestamp"])

    private val _taskResult = MutableStateFlow<WrittenTaskResultEntity?>(null)
    val taskResult = _taskResult.asStateFlow()

    init {
        viewModelScope.launch {
            _taskResult.value = writtenTaskDao.getResultByTimestamp(timestamp)
        }
    }
}
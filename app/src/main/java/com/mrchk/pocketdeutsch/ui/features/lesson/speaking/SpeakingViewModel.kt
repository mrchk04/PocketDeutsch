package com.mrchk.pocketdeutsch.ui.features.lesson.speaking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.SpeakingPractice
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeakingViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _speakingData = MutableStateFlow<SpeakingPractice?>(null)
    val speakingData = _speakingData.asStateFlow()

    private val _timeLeft = MutableStateFlow(15 * 60L)
    val timeLeft = _timeLeft.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val lesson = lessonRepository.getLessonById(lessonId)
            _speakingData.value = lesson?.examPractice?.speaking

            if (_speakingData.value != null) {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
            }
        }
    }

    fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }

    fun completeExerciseNode() {
        viewModelScope.launch {
            val nodeId = "${lessonId}_speaking"
            lessonRepository.completeNode(nodeId)
        }
    }
}
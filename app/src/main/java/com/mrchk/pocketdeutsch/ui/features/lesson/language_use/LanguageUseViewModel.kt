package com.mrchk.pocketdeutsch.ui.features.lesson.language_use

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.GapOption
import com.mrchk.pocketdeutsch.domain.model.LanguageUsePractice
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageUseViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _allExercises = MutableStateFlow<List<LanguageUsePractice>?>(null)
    val allExercises = _allExercises.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _exercise = MutableStateFlow<LanguageUsePractice?>(null)
    val exercise = _exercise.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val selectedAnswers = _selectedAnswers.asStateFlow()

    private val _activeGap = MutableStateFlow<GapOption?>(null)
    val activeGap = _activeGap.asStateFlow()

    private val _isChecked = MutableStateFlow(false)
    val isChecked = _isChecked.asStateFlow()

    init {
        loadExerciseData()
    }

    private fun loadExerciseData() {
        viewModelScope.launch {
            try {
                val lesson = lessonRepository.getLessonById(lessonId)
                val exercises = lesson?.examPractice?.languageUse ?: emptyList()

                _allExercises.value = exercises
            } catch (e: Exception) {
                _allExercises.value = emptyList()
            }
        }
    }

    fun moveToNextStep(onAllFinished: () -> Unit) {
        val totalSize = _allExercises.value?.size ?: 0
        if (_currentIndex.value < totalSize - 1) {
            _currentIndex.value += 1
            _selectedAnswers.value = emptyMap()
            _isChecked.value = false
        } else {
            onAllFinished()
        }
    }

    fun onGapClick(gapOption: GapOption) {
        if (!_isChecked.value) {
            _activeGap.value = gapOption
        }
    }

    fun selectOption(gapNumber: Int, option: String) {
        _selectedAnswers.value = _selectedAnswers.value.toMutableMap().apply {
            put(gapNumber, option)
        }
        _activeGap.value = null
    }

    fun checkAnswers() {
        _isChecked.value = true
    }

    fun dismissGapSelection() {
        _activeGap.value = null
    }

    fun completeExerciseNode(lessonId: String) {
        viewModelScope.launch {
            val nodeId = "${lessonId}_language_use"
            lessonRepository.completeNode(nodeId)
        }
    }
}
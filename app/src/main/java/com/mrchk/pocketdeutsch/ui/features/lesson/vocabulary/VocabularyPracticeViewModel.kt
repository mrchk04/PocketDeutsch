package com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.VocabExercise
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyPracticeViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _exercises = MutableStateFlow<List<VocabExercise>>(emptyList())
    val exercises = _exercises.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            val lesson = lessonRepository.getLessonById(lessonId)
            _exercises.value = lesson?.vocabulary?.exercises ?: emptyList()
        }
    }

    fun nextExercise() {
        if (_currentIndex.value < _exercises.value.size - 1) {
            _currentIndex.value += 1
        }
    }

    fun getProgress(): Float {
        if (_exercises.value.isEmpty()) return 0f
        return (_currentIndex.value + 1).toFloat() / _exercises.value.size.toFloat()
    }

    fun resetPractice() {
        _currentIndex.value = 0
    }

    fun completeVocabularyNode(lessonId: String) {
        viewModelScope.launch {
            val nodeId = "${lessonId}_vocabulary"
            lessonRepository.completeNode(nodeId)
        }
    }
}
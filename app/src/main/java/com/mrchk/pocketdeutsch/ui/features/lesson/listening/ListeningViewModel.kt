package com.mrchk.pocketdeutsch.ui.features.lesson.listening

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.ListeningPractice
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListeningViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _listeningData = MutableStateFlow<ListeningPractice?>(null)
    val listeningData = _listeningData.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer = _selectedAnswer.asStateFlow()

    private val _isChecked = MutableStateFlow(false)
    val isChecked = _isChecked.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val lesson = lessonRepository.getLessonById(lessonId)
            // Шлях до аудіювання. Переконайся, що він збігається з твоєю моделлю
            _listeningData.value = lesson?.examPractice?.listening
        }
    }

    fun selectAnswer(answer: String) {
        if (!_isChecked.value) {
            _selectedAnswer.value = answer
        }
    }

    fun checkAnswer() {
        _isChecked.value = true
    }

    fun nextQuestion() {
        val data = _listeningData.value ?: return
        if (_currentQuestionIndex.value < data.exercise.items.size - 1) {
            _currentQuestionIndex.value += 1
            _selectedAnswer.value = null
            _isChecked.value = false
        }
    }

    fun completeExerciseNode() {
        viewModelScope.launch {
            val nodeId = "${lessonId}_listening"
            lessonRepository.completeNode(nodeId)
        }
    }
}
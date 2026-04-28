package com.mrchk.pocketdeutsch.ui.features.lesson.reading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.ReadingPractice
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _readingData = MutableStateFlow<ReadingPractice?>(null)
    val readingData = _readingData.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer = _selectedAnswer.asStateFlow()

    private val _isChecked = MutableStateFlow(false)
    val isChecked = _isChecked.asStateFlow()

    private val _usedAnswers = MutableStateFlow<Set<String>>(emptySet())
    val usedAnswers = _usedAnswers.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val lesson = lessonRepository.getLessonById(lessonId)
            // Припускаємо, що reading лежить тут. Зміни шлях, якщо у тебе інакше.
            _readingData.value = lesson?.examPractice?.reading
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
        val data = _readingData.value ?: return
        // Звертаємося через .exercise
        if (_currentQuestionIndex.value < data.exercise.items.size - 1) {
            _selectedAnswer.value?.let {
                _usedAnswers.value = _usedAnswers.value + it
            }

            _currentQuestionIndex.value += 1
            _selectedAnswer.value = null
            _isChecked.value = false
        }
    }

    fun completeExerciseNode() {
        viewModelScope.launch {
            val nodeId = "${lessonId}_reading"
            lessonRepository.completeNode(nodeId)
        }
    }
}
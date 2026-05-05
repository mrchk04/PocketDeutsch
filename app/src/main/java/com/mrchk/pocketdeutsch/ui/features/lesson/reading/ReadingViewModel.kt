package com.mrchk.pocketdeutsch.ui.features.lesson.reading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.InteractiveExercise
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

    // Індекс поточної вправи (0 - оголошення, 1 - стаття)
    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex = _currentExerciseIndex.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer = _selectedAnswer.asStateFlow()

    // Для вправ з вводом тексту (Exercise 2)
    private val _userTextAnswer = MutableStateFlow("")
    val userTextAnswer = _userTextAnswer.asStateFlow()

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
            _readingData.value = lesson?.examPractice?.reading
        }
    }

    // Для вибору варіантів (A, B, C, Keine)
    fun selectAnswer(answer: String) {
        if (!_isChecked.value) {
            _selectedAnswer.value = answer
        }
    }

    // Для введення тексту у другій вправі
    fun onUserTextChange(text: String) {
        if (!_isChecked.value) {
            _userTextAnswer.value = text
        }
    }

    fun checkAnswer() {
        _isChecked.value = true
    }

    fun nextQuestion() {
        val data = _readingData.value ?: return
        val currentEx = data.exercises.getOrNull(_currentExerciseIndex.value)
                as? InteractiveExercise.Standard ?: return

        if (_currentQuestionIndex.value < currentEx.items.size - 1) {
            _selectedAnswer.value?.let {
                _usedAnswers.value = _usedAnswers.value + it
            }
            _currentQuestionIndex.value += 1
            resetStateForNextItem()
        } else if (_currentExerciseIndex.value < data.exercises.size - 1) {
            _currentExerciseIndex.value += 1
            _currentQuestionIndex.value = 0
            _usedAnswers.value = emptySet()
            resetStateForNextItem()
        } else {
            completeExerciseNode()
        }
    }

    private fun resetStateForNextItem() {
        _selectedAnswer.value = null
        _userTextAnswer.value = ""
        _isChecked.value = false
    }

    fun completeExerciseNode() {
        viewModelScope.launch {
            val nodeId = "${lessonId}_reading"
            lessonRepository.completeNode(nodeId)
        }
    }
}
package com.mrchk.pocketdeutsch.ui.features.lesson.listening

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.InteractiveExercise
import com.mrchk.pocketdeutsch.domain.model.ListeningPractice
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListeningScreenState(
    val isLoading: Boolean = true,
    val practice: ListeningPractice? = null,
    val currentQuestionIndex: Int = 0,
    val currentExerciseIndex: Int = 0,
    val exercises: List<ListeningPractice> = emptyList(),
    val userAnswers: Map<Int, String> = emptyMap(),
    val evaluationResults: Map<Int, Boolean> = emptyMap(),
    val isChecked: Boolean = false,
    val isFinished: Boolean = false
)

@HiltViewModel
class ListeningViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _state = MutableStateFlow(ListeningScreenState())
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val lesson = lessonRepository.getLessonById(lessonId)
            val listeningPractice = lesson?.examPractice?.listening

            _state.update {
                it.copy(
                    isLoading = false,
                    practice = listeningPractice
                )
            }
        }
    }

    fun selectAnswer(answer: String) {
        val currentState = _state.value
        if (!currentState.isChecked) {
            val updatedAnswers = currentState.userAnswers.toMutableMap()
            updatedAnswers[currentState.currentQuestionIndex] = answer

            _state.update { it.copy(userAnswers = updatedAnswers) }
        }
    }

    fun checkAnswer() {
        val state = _state.value
        val practice = state.practice ?: return
        val currentEx = practice.exercises.getOrNull(state.currentExerciseIndex) ?: return

        val newResults = mutableMapOf<Int, Boolean>()

        val totalQuestions = when (currentEx) {
            is InteractiveExercise.MultipleChoice -> currentEx.questions.size
            is InteractiveExercise.RichtigFalsch -> currentEx.items.size
            is InteractiveExercise.Standard -> currentEx.items.size
            else -> 0
        }

        for (i in 0 until totalQuestions) {
            val userAnswer = state.userAnswers[i]
            if (userAnswer != null) {
                val isCorrect = when (currentEx) {
                    is InteractiveExercise.RichtigFalsch -> {
                        val correct = currentEx.answers.getOrNull(i) ?: ""
                        userAnswer.take(1).equals(correct, ignoreCase = true)
                    }
                    is InteractiveExercise.MultipleChoice -> {
                        val correct = currentEx.questions.getOrNull(i)?.answer ?: ""
                        userAnswer.startsWith(correct, ignoreCase = true)
                    }
                    else -> false
                }
                newResults[i] = isCorrect
            }
        }

        _state.update {
            it.copy(
                isChecked = true,
                evaluationResults = newResults
            )
        }
    }

    fun nextQuestion() {
        val currentState = _state.value
        val practice = currentState.practice ?: return

        val currentEx = practice.exercises.getOrNull(currentState.currentExerciseIndex) ?: return

        val totalQuestions = when (currentEx) {
            is InteractiveExercise.MultipleChoice -> currentEx.questions.size
            is InteractiveExercise.RichtigFalsch -> currentEx.items.size
            is InteractiveExercise.Standard -> currentEx.items.size
            else -> 0
        }

        if (currentState.currentQuestionIndex < totalQuestions - 1) {
            _state.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    isChecked = false
                )
            }
        } else {
            _state.update { it.copy(isFinished = true) }
            completeExerciseNode()
        }
    }

    fun selectAnswerForIndex(index: Int, answer: String) {
        _state.update { currentState ->
            val updatedAnswers = currentState.userAnswers.toMutableMap()
            updatedAnswers[index] = answer
            currentState.copy(userAnswers = updatedAnswers)
        }
    }

    fun nextExercise() {
        _state.update { currentState ->
            currentState.copy(
                currentExerciseIndex = currentState.currentExerciseIndex + 1,
                currentQuestionIndex = 0,
                userAnswers = emptyMap(),
                evaluationResults = emptyMap(),
                isChecked = false
            )
        }
    }

    fun completeExerciseNode() {
        viewModelScope.launch {
            val nodeId = "${lessonId}_listening"
            lessonRepository.completeNode(nodeId)
        }
    }
}
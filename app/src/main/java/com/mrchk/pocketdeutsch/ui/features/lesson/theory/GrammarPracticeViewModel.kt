package com.mrchk.pocketdeutsch.ui.features.lesson.theory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.data.repository.GeminiRepository
import com.mrchk.pocketdeutsch.domain.model.InteractiveExercise
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GrammarPracticeViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val geminiRepository: GeminiRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _uiState = MutableStateFlow(GrammarPracticeUiState())
    val uiState: StateFlow<GrammarPracticeUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val lesson = lessonRepository.getLessonById(lessonId)
                if (lesson != null && lesson.grammar.exercises.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            exercises = lesson.grammar.exercises
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateAnswer(itemIndex: Int, text: String) {
        _uiState.update { state ->
            val updatedAnswers = state.userAnswers.toMutableMap()
            updatedAnswers[itemIndex] = text
            state.copy(userAnswers = updatedAnswers, isChecked = false)
        }
    }

    fun checkAnswers() {
        val state = _uiState.value
        val currentExercise = state.currentExercise ?: return

        if (currentExercise.type == "free_production") {
            val userAnswer = state.userAnswers[0] ?: ""

            evaluateTextWithAi(
                instruction = currentExercise.instruction,
                text = userAnswer
            )
            return
        }

        val results = mutableMapOf<Int, Boolean>()

        currentExercise.items.forEachIndexed { index, _ ->
            val rawUserAnswer = state.userAnswers[index] ?: ""
            val rawCorrectAnswer = currentExercise.answers.getOrNull(index) ?: ""

            val cleanUserAnswer = cleanString(rawUserAnswer)
            val cleanCorrectAnswer = cleanString(rawCorrectAnswer)

            results[index] = cleanUserAnswer.equals(cleanCorrectAnswer, ignoreCase = true)
        }

        _uiState.update { it.copy(evaluationResults = results, isChecked = true) }
    }

    fun nextExercise() {
        val state = _uiState.value
        if (state.currentIndex < state.exercises.lastIndex) {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    userAnswers = emptyMap(),
                    evaluationResults = emptyMap(),
                    isChecked = false
                )
            }
        } else {
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    private fun cleanString(input: String): String {
        return input
            .replaceFirst(Regex("^\\d+\\.\\s*"), "") // Відрізаємо нумерацію ("1. ")
            .replace(Regex("[.,!?]+$"), "") // Відрізаємо розділові знаки в кінці рядка
            .replace(Regex("\\s+"), " ") // Перетворюємо будь-які подвійні/потрійні пробіли на один
            .trim()
    }

    private fun evaluateTextWithAi(instruction: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isEvaluatingAi = true, isChecked = true) }

            try {
                val result = geminiRepository.checkSimpleGrammar(instruction, text)

                val finalFeedback = """
                    ${result.feedback}
                    
                    Правильний варіант:
                    ${result.correctedText}
                """.trimIndent()

                _uiState.update { state ->
                    val updatedResults = state.evaluationResults.toMutableMap()
                    updatedResults[0] = result.isCorrect

                    state.copy(
                        isEvaluatingAi = false,
                        aiFeedbackText = finalFeedback,
                        evaluationResults = updatedResults
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isEvaluatingAi = false,
                        aiFeedbackText = "Помилка перевірки ШІ: ${e.localizedMessage}. Спробуйте ще раз."
                    )
                }
            }
        }
    }

    fun completeGrammarNode(lessonId: String) {
        viewModelScope.launch {
            val nodeId = "${lessonId}_grammar"
            lessonRepository.completeNode(nodeId)
        }
    }

    fun resetProgress() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                userAnswers = emptyMap(),
                evaluationResults = emptyMap(),
                isChecked = false,
                isFinished = false,
                aiFeedbackText = null
            )
        }
    }
}

data class GrammarPracticeUiState(
    val isLoading: Boolean = true,
    val exercises: List<InteractiveExercise> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, String> = emptyMap(),
    val evaluationResults: Map<Int, Boolean> = emptyMap(),
    val isChecked: Boolean = false,
    val isFinished: Boolean = false,
    val isEvaluatingAi: Boolean = false,
    val aiFeedbackText: String? = null
) {
    val currentExercise: InteractiveExercise?
        get() = exercises.getOrNull(currentIndex)
}

package com.mrchk.pocketdeutsch.ui.features.lesson.theory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.model.GrammarSection
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TheoryUiState {
    object Loading : TheoryUiState()
    data class Success(val grammar: GrammarSection) : TheoryUiState()
    data class Error(val message: String) : TheoryUiState()
}

@HiltViewModel
class TheoryViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    private val _uiState = MutableStateFlow<TheoryUiState>(TheoryUiState.Loading)
    val uiState: StateFlow<TheoryUiState> = _uiState.asStateFlow()

    init {
        loadTheory()
    }

    private fun loadTheory() {
        viewModelScope.launch {
            _uiState.value = TheoryUiState.Loading
            try {
                val lesson = lessonRepository.getLessonById(lessonId)
                if (lesson != null) {
                    _uiState.value = TheoryUiState.Success(lesson.grammar)
                } else {
                    _uiState.value = TheoryUiState.Error("Урок не знайдено")
                }
            } catch (e: Exception) {
                _uiState.value = TheoryUiState.Error(e.message ?: "Помилка завантаження")
            }
        }
    }
}
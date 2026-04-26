package com.mrchk.pocketdeutsch.ui.features.lesson.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.domain.model.CourseNode
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])
    private val _uiState = MutableStateFlow<LessonDetailState>(LessonDetailState.Loading)
    val uiState: StateFlow<LessonDetailState> = _uiState.asStateFlow()

    init {
        loadLessonData(lessonId)
    }

    private fun loadLessonData(lessonId: String) {
        viewModelScope.launch {
            _uiState.value = LessonDetailState.Loading
            try {
                val lesson = lessonRepository.getLessonById(lessonId)
                if (lesson == null) {
                    _uiState.value = LessonDetailState.Error("Урок не знайдено")
                    return@launch
                }

                lessonRepository.getLessonPathway(lessonId).collectLatest { dbNodes ->
                    if (dbNodes.isNotEmpty()) {
                        val pathwayNodes = dbNodes.toPathwayNodes()

                        _uiState.value = LessonDetailState.Success(lesson, pathwayNodes)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = LessonDetailState.Error(e.message ?: "Помилка завантаження")
            }
        }
    }
}

private fun List<CourseNode>.toPathwayNodes(): List<PathwayNodeData> {
    var firstUncompletedFound = false

    return this.map { node ->
        val nodeState = when {
            node.isCompleted -> NodeState.COMPLETED
            !firstUncompletedFound -> {
                firstUncompletedFound = true
                NodeState.ACTIVE
            }
            else -> NodeState.NOT_STARTED
        }

        val (iconRes, subtitle) = when (node.type) {
            "vocabulary" -> R.drawable.ic_book_open_text_bold to "Нові слова та вирази"
            "grammar" -> R.drawable.ic_book_bookmark_bold to "Граматичні правила"
            "reading" -> R.drawable.ic_text_aa_bold to "Робота з текстами"
            "listening" -> R.drawable.ic_text_aa_bold to "Розуміння на слух"
            "language_use" -> R.drawable.ic_pencil_simple_bold to "Sprachbausteine"
            "writing" -> R.drawable.ic_pencil_simple_bold to "Письмовий формат"
            "speaking" -> R.drawable.ic_book_open_text_bold to "Усна практика"
            else -> R.drawable.ic_book_bookmark_bold to ""
        }

        PathwayNodeData(
            id = node.id,
            title = node.title,
            subtitle = subtitle,
            iconRes = iconRes,
            state = nodeState,
            type = node.type,
        )
    }
}

sealed class LessonDetailState {
    object Loading : LessonDetailState()
    data class Success(val lesson: Lesson, val nodes: List<PathwayNodeData>) : LessonDetailState()
    data class Error(val message: String) : LessonDetailState()
}
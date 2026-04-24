package com.mrchk.pocketdeutsch.ui.features.lesson.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        loadLesson(lessonId)

//        val idFromNav: String? = savedStateHandle["lessonId"]
//        android.util.Log.d("DEBUG_NAV", "ID отриманий з навігації: $idFromNav")
//        loadLesson(idFromNav ?: "unknown")

    }

    private fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            _uiState.value = LessonDetailState.Loading
            try {
                val lesson = lessonRepository.getLessonById(lessonId)
                if (lesson != null) {
                    val nodes = createPathwayNodes(lesson)
                    _uiState.value = LessonDetailState.Success(lesson, nodes)
                } else {
                    _uiState.value = LessonDetailState.Error("Урок не знайдено")
                }
            } catch (e: Exception) {
                _uiState.value = LessonDetailState.Error(e.message ?: "Помилка завантаження")
            }
        }
    }

    private fun createPathwayNodes(lesson: Lesson): List<PathwayNodeData> {
        val nodes = mutableListOf<PathwayNodeData>()

        // 1. Теорія та Граматика (Є завжди)
        nodes.add(
            PathwayNodeData(
                id = "theory",
                title = "Теорія та Граматика",
                subtitle = lesson.grammar.topic,
                iconRes = R.drawable.ic_book_bookmark_bold,
                state = NodeState.COMPLETED // Статуси поки залишаємо для краси UI
            )
        )

        // 2. Словник (Є завжди)
        nodes.add(
            PathwayNodeData(
                id = "wortschatz",
                title = "Словник (Wortschatz)",
                subtitle = "Нові слова та вирази",
                iconRes = R.drawable.ic_book_open_text_bold,
                state = NodeState.ACTIVE
            )
        )

        // 3. Читання (Є завжди в екзаменаційному блоці)
        nodes.add(
            PathwayNodeData(
                id = "leseverstehen",
                title = "Читання (Leseverstehen)",
                subtitle = "Робота з текстами",
                iconRes = R.drawable.ic_text_aa_bold,
                state = NodeState.NOT_STARTED
            )
        )

        // 4. Аудіювання (НОВЕ)
        nodes.add(
            PathwayNodeData(
                id = "hoerverstehen",
                title = "Аудіювання (Hörverstehen)",
                subtitle = "Розуміння на слух",
                // Якщо іконки немає, заміни на існуючу (напр. ic_headphones)
                iconRes = R.drawable.ic_text_aa_bold,
                state = NodeState.NOT_STARTED
            )
        )

        // 5. Мовні конструкції (ДИНАМІЧНО: додаємо тільки якщо вони є)
        if (lesson.examPractice.languageUse.isNotEmpty()) {
            nodes.add(
                PathwayNodeData(
                    id = "sprachbausteine",
                    title = "Мовні конструкції",
                    subtitle = "Sprachbausteine",
                    iconRes = R.drawable.ic_pencil_simple_bold,
                    state = NodeState.NOT_STARTED
                )
            )
        }

        // 6. Письмо (Ніяких елвіс-операторів, бо ми зробили поле обов'язковим)
        nodes.add(
            PathwayNodeData(
                id = "schreiben",
                title = "Письмо (Schreiben)",
                subtitle = lesson.examPractice.writing.format.replace("_", " ")
                    .replaceFirstChar { it.uppercase() },
                iconRes = R.drawable.ic_pencil_simple_bold,
                state = NodeState.NOT_STARTED
            )
        )

        // 7. Говоріння (НОВЕ)
        nodes.add(
            PathwayNodeData(
                id = "sprechen",
                title = "Говоріння (Sprechen)",
                subtitle = lesson.examPractice.speaking.taskType.replace("_", " ")
                    .replaceFirstChar { it.uppercase() },
                // Заміни на свою іконку (напр. мікрофон)
                iconRes = R.drawable.ic_book_open_text_bold,
                state = NodeState.NOT_STARTED
            )
        )

        return nodes
    }
}

sealed class LessonDetailState {
    object Loading : LessonDetailState()
    data class Success(val lesson: Lesson, val nodes: List<PathwayNodeData>) : LessonDetailState()
    data class Error(val message: String) : LessonDetailState()
}
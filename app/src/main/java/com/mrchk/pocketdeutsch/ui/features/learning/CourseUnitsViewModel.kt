package com.mrchk.pocketdeutsch.ui.features.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CourseUnitsUiState {
    object Loading : CourseUnitsUiState()
    data class Success(
        val units: List<UnitData>,
        val availableLevels: List<String>
    ) : CourseUnitsUiState()
    data class Error(val message: String) : CourseUnitsUiState()
}

@HiltViewModel
class CourseUnitsViewModel @Inject constructor(
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CourseUnitsUiState>(CourseUnitsUiState.Loading)
    val uiState: StateFlow<CourseUnitsUiState> = _uiState.asStateFlow()

    private val _selectedLevel = MutableStateFlow("")
    val selectedLevel: StateFlow<String> = _selectedLevel.asStateFlow()

    init {
        loadCourseData()
    }

    private fun loadCourseData() {
        viewModelScope.launch {
            _uiState.value = CourseUnitsUiState.Loading
            try {

                val allLessons = lessonRepository.getLessons()

                if (allLessons.isEmpty()) {
                    _uiState.value = CourseUnitsUiState.Error("Список уроків порожній")
                    return@launch
                }

                val mappedUnits = allLessons.mapIndexed { index, lesson ->
                    val completed = lessonRepository.getCompletedTasksCount(lesson.lessonId)
                    val total = lesson.totalTasks

                    val state = when {
                        completed == total -> UnitState.COMPLETED
                        completed > 0 -> UnitState.ACTIVE
                        else -> UnitState.ACTIVE
                    }

                    UnitData(
                        id = lesson.lessonId,
                        level = lesson.level,
                        unitNumber = "Модуль ${index + 1}",
                        title = lesson.title,
                        description = lesson.description,
                        completedLessons = completed, // ТЕПЕР ДИНАМІЧНО
                        totalLessons = total,         // ТЕПЕР ДИНАМІЧНО
                        state = state,                // ТЕПЕР ДИНАМІЧНО
                        isExam = false
                        // progress = completed.toFloat() / total.toFloat() // Якщо додала це поле в UnitData
                    )
                }

                // Динамічно збираємо всі унікальні рівні з наших даних
                val levels = mappedUnits.map { it.level }.distinct().sorted()

                _uiState.value = CourseUnitsUiState.Success(
                    units = mappedUnits,
                    availableLevels = levels
                )

                if (_selectedLevel.value.isEmpty() && levels.isNotEmpty()) {
                    _selectedLevel.value = levels.first()
                }
            } catch (e: Exception) {
                _uiState.value = CourseUnitsUiState.Error(e.message ?: "Помилка завантаження курсу")
            }
        }
    }

    fun selectLevel(level: String) {
        _selectedLevel.value = level
    }
}
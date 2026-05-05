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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadCourseData()
    }

    fun refreshData() {
        loadCourseData(isSilent = true)
    }

    private fun loadCourseData(isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent) {
                _uiState.value = CourseUnitsUiState.Loading
            }

            // ВМИКАЄМО ІНДИКАТОР ТУТ
            _isRefreshing.value = true

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
                        description = lesson.shortDescription,
                        completedLessons = completed,
                        totalLessons = total,
                        state = state,
                        isExam = false,
                    )
                }

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
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun resetUnitProgress(unitId: String) {
        viewModelScope.launch {
            try {
                lessonRepository.resetUnitProgress(unitId)
                loadCourseData(isSilent = true)
            } catch (e: Exception) {
                _uiState.value = CourseUnitsUiState.Error("Не вдалося скинути прогрес: ${e.message}")
            }
        }
    }

    fun selectLevel(level: String) {
        _selectedLevel.value = level
    }
}
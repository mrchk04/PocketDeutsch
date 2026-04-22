package com.mrchk.pocketdeutsch.ui.features.learning

enum class UnitState { COMPLETED, ACTIVE, NOT_STARTED }

data class UnitData(
    val id: String,
    val level: String,
    val unitNumber: String,
    val title: String,
    val description: String,
    val completedLessons: Int,
    val totalLessons: Int,
    val state: UnitState,
    val isExam: Boolean = false
) {
    val progress: Float
        get() = if (totalLessons > 0) completedLessons.toFloat() / totalLessons.toFloat() else 0f
}
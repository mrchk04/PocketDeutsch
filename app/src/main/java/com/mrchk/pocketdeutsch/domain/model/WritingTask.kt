package com.mrchk.pocketdeutsch.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ProficiencyLevel {
    A1, A2, B1, B2, C1, C2
}

@Serializable
data class WritingTask(
    val id: String,
    val level: ProficiencyLevel,
    val title: String,
    val promptText: String,
    val minWords: Int,
    val requiredPoints: List<TaskRequirement>,
    val hints: List<String> = emptyList()
)

@Serializable
data class TaskRequirement(
    val id: String,
    val text: String,
    val isChecked: Boolean = false
)
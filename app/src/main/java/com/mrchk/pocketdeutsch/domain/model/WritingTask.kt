package com.mrchk.pocketdeutsch.domain.model

// Enum для рівнів складності (допоможе ШІ розуміти, наскільки суворо оцінювати)
enum class ProficiencyLevel {
    A1, A2, B1, B2, C1, C2
}

// Головна модель вправи
data class WritingTask(
    val id: String,
    val level: ProficiencyLevel,
    val title: String, // Наприклад: "E-Mail an Marianne"
    val promptText: String, // Сам текст листа від Маріанни
    val minWords: Int, // Ліміт слів (напр., 30 для A1, 80 для B1)
    val requiredPoints: List<TaskRequirement>, // Наш чекліст
    val hints: List<String> = emptyList() // Redemittel (пустий список для складних рівнів)
)

// Пункт чекліста (Leitpunkt)
data class TaskRequirement(
    val id: String,
    val text: String, // "welche Ausflüge Sie mit Marianne machen wollen"
    val isChecked: Boolean = false // Стан для UI
)
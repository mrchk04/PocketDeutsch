package com.mrchk.pocketdeutsch.domain.model

// Головний результат перевірки
data class AiEvaluationResult(
    val score: Int, // Оцінка від 0 до 100
    val overallFeedback: String, // Дружній коментар ("Чудова робота! Ти гарно розкрила тему, але...")
    val checklistEvaluations: List<ChecklistEvaluation>, // Розбір пунктів
    val textCorrections: List<TextCorrection> // Конкретні помилки
)

// Оцінка виконання конкретного пункту чекліста
data class ChecklistEvaluation(
    val requirementId: String, // Збігається з id з TaskRequirement
    val isFulfilled: Boolean, // Чи розкрив юзер цей пункт
    val comment: String // Пояснення ШІ (напр. "Ти забула написати про одяг")
)

// Модель для підсвітки та пояснення помилок
data class TextCorrection(
    val originalIncorrectText: String, // Як написав юзер (напр. "Ich komme dich")
    val suggestedCorrection: String, // Як правильно (напр. "Ich besuche dich")
    val explanation: String, // Пояснення правила (напр. "Дієслово besuchen вимагає Akkusativ")

    // 🔥 ДЛЯ ПІДСВІТКИ В UI:
    val startIndex: Int, // З якого символу починається помилка в оригінальному тексті
    val endIndex: Int // На якому символі закінчується
)
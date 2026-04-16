package com.mrchk.pocketdeutsch.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AiEvaluationResult(
    val score: Int,
    val grammarScore: Int = 0,
    val vocabularyScore: Int = 0,
    val contentScore: Int = 0,
    val overallFeedback: String,
    val checklistEvaluations: List<ChecklistEvaluation>,
    val textCorrections: List<TextCorrection>
)

@Serializable
data class ChecklistEvaluation(
    val requirementId: String,
    val isFulfilled: Boolean,
    val comment: String
)

@Serializable
data class TextCorrection(
    val originalIncorrectText: String,
    val suggestedCorrection: String,
    val explanation: String,

    val startIndex: Int,
    val endIndex: Int
)
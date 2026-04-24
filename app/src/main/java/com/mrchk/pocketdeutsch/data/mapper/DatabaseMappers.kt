package com.mrchk.pocketdeutsch.data.mapper

import com.mrchk.pocketdeutsch.data.local.WrittenTaskResultEntity
import com.mrchk.pocketdeutsch.domain.model.AiEvaluationResult

fun WrittenTaskResultEntity.toUiModel(): AiEvaluationResult {
    return AiEvaluationResult(
        score = this.overallScore,
        grammarScore = this.grammarScore,
        vocabularyScore = this.vocabularyScore,
        contentScore = this.contentScore,
        overallFeedback = this.overallFeedback,
        checklistEvaluations = this.checklistEvaluations,
        textCorrections = this.corrections
    )
}
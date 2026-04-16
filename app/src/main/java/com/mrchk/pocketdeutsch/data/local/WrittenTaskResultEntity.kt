package com.mrchk.pocketdeutsch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrchk.pocketdeutsch.domain.model.ChecklistEvaluation
import com.mrchk.pocketdeutsch.domain.model.TextCorrection

@Entity(tableName = "written_task_result")
data class WrittenTaskResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exerciseId: String,
    val originalText: String,
    val overallScore: Int,
    val grammarScore: Int,
    val vocabularyScore: Int,
    val contentScore: Int,
    val overallFeedback: String,
    val checklistEvaluations: List<ChecklistEvaluation>,
    val corrections: List<TextCorrection>,
    val timestamp: Long = System.currentTimeMillis(),
    val pendingSync: Boolean = true
)
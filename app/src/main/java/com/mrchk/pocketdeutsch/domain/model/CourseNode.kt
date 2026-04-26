package com.mrchk.pocketdeutsch.domain.model

data class CourseNode(
    val id: String,
    val lessonId: String,
    val title: String,
    val type: String,
    val orderIndex: Int,
    val isCompleted: Boolean
)
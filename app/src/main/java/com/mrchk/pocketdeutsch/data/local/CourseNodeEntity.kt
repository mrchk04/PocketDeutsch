package com.mrchk.pocketdeutsch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_nodes")
data class CourseNodeEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val title: String,
    val type: String,
    val orderIndex: Int,
    val isCompleted: Boolean = false,
    val bestScore: Int = 0,
    val lastScore: Int = 0
)
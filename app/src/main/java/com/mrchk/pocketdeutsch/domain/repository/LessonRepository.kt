package com.mrchk.pocketdeutsch.domain.repository

import com.mrchk.pocketdeutsch.domain.model.CourseNode
import com.mrchk.pocketdeutsch.domain.model.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    suspend fun getLessons(): List<Lesson>
    suspend fun getLessonById(id: String): Lesson?

    fun getLessonPathway(lessonId: String): Flow<List<CourseNode>>
    suspend fun completeNode(nodeId: String)
    suspend fun getCompletedTasksCount(lessonId: String): Int
}
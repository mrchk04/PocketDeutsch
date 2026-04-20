package com.mrchk.pocketdeutsch.domain.repository

import com.mrchk.pocketdeutsch.domain.model.Lesson

interface LessonRepository {
    suspend fun getLessons(): List<Lesson>
    suspend fun getLessonById(id: String): Lesson?
}
package com.mrchk.pocketdeutsch.data.repository

import android.content.Context
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import javax.inject.Inject

class LocalLessonRepositoryImpl @Inject constructor(
    @ApplicationContext private val  context: Context
) : LessonRepository{
    private val json = Json { ignoreUnknownKeys = true}

    override suspend fun getLessons(): List<Lesson> = withContext(Dispatchers.IO){
        try {
            val jsonString = context.assets.open("pocket_deutsch_lessons_v2.json")
                .bufferedReader()
                .use { it.readText() }

            json.decodeFromString<List<Lesson>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getLessonById(id: String): Lesson? {
        val lessons = getLessons()
        return lessons.find { it.lessonId == id }
    }
}
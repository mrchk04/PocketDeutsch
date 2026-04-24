package com.mrchk.pocketdeutsch.data.repository

import android.content.Context
import com.mrchk.pocketdeutsch.data.local.dto.ModuleResponse
import com.mrchk.pocketdeutsch.data.mapper.toDomainModel
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import javax.inject.Inject

class LocalLessonRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LessonRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("pocket_deutsch_module.json")
                .bufferedReader()
                .use { it.readText() }

            // 1. Парсимо JSON у наш список DTO класів
            val dtoList = json.decodeFromString<List<ModuleResponse>>(jsonString)

            // 2. Викликаємо мапер для кожного елемента і перетворюємо в Domain Model
            dtoList.map { it.module.toDomainModel() }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getLessonById(id: String): Lesson? {
        val lessons = getLessons()

        lessons.forEach {
            android.util.Log.d("DEBUG_REPO", "Шукаємо: '$id', Маємо в базі: '${it.lessonId}'")
        }

        return lessons.find { it.lessonId == id }
    }
}
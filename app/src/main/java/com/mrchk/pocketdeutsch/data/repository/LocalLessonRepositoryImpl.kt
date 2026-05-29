package com.mrchk.pocketdeutsch.data.repository

import android.content.Context
import com.mrchk.pocketdeutsch.data.local.CourseNodeDao
import com.mrchk.pocketdeutsch.data.local.CourseNodeEntity
import com.mrchk.pocketdeutsch.data.local.dto.ModuleResponse
import com.mrchk.pocketdeutsch.data.mapper.toDomain
import com.mrchk.pocketdeutsch.data.mapper.toDomainModel
import com.mrchk.pocketdeutsch.domain.model.CourseNode
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocalLessonRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val courseNodeDao: CourseNodeDao
) : LessonRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        // Обертаємо весь блок у загальний try-catch на випадок глобальних помилок
        try {
            val assetManager = context.assets
            // Якщо твої файли лежать прямо в папці assets, залишаємо ""
            // Якщо ти поклала їх у підпапку, наприклад assets/json, напиши list("json")
            val allFiles = assetManager.list("") ?: emptyArray()

            val moduleFiles = allFiles
                .filter { it.startsWith("module_") && it.endsWith(".json") }
                .sorted()

            android.util.Log.d("DEBUG_REPO", "Знайдено файлів: ${moduleFiles.size} -> $moduleFiles")

            val allLessons = mutableListOf<Lesson>()

            for (fileName in moduleFiles) {
                // Окремий try-catch ДЛЯ КОЖНОГО ФАЙЛУ
                try {
                    android.util.Log.d("DEBUG_REPO", "Читаємо файл: $fileName")

                    val jsonString = assetManager.open(fileName)
                        .bufferedReader()
                        .use { it.readText() }

                    val dtoList = json.decodeFromString<List<ModuleResponse>>(jsonString)
                    val lessons = dtoList.map { it.module.toDomainModel() }

                    allLessons.addAll(lessons)
                    android.util.Log.d("DEBUG_REPO", "✅ Успішно розпарсено: $fileName")

                } catch (e: Exception) {
                    // Якщо в JSON помилка, ми побачимо це в логах, але інші файли продовжать вантажитись
                    android.util.Log.e("DEBUG_REPO", "❌ Помилка парсингу у файлі $fileName: ${e.message}")
                    e.printStackTrace()
                }
            }

            // Записуємо в базу те, що вдалося успішно прочитати
            seedDatabaseIfNeeded(allLessons)

            allLessons

        } catch (e: Exception) {
            android.util.Log.e("DEBUG_REPO", "Глобальна помилка: ${e.message}")
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

    override fun getLessonPathway(lessonId: String): Flow<List<CourseNode>> {
        return courseNodeDao.getNodesForLesson(lessonId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun completeNode(nodeId: String) {
        courseNodeDao.markNodeAsCompleted(nodeId)
    }

    suspend fun completeNodeWithScore(nodeId: String, score: Int) {
        courseNodeDao.completeNodeWithScore(nodeId, score)
    }

    override suspend fun getCompletedTasksCount(lessonId: String): Int {
        return courseNodeDao.getCompletedNodesCount(lessonId)
    }

    override suspend fun resetUnitProgress(lessonId: String, nodeId: String?) = withContext(Dispatchers.IO) {
        try {
            if (nodeId != null) {
                courseNodeDao.resetNodeCompletion(nodeId)
            } else {
                courseNodeDao.resetLessonCompletion(lessonId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun seedDatabaseIfNeeded(lessons: List<Lesson>) {
        lessons.forEach { lesson ->
            val lessonId = lesson.lessonId

            val existingNodesCount = courseNodeDao.getCompletedNodesCount(lessonId)

            if (existingNodesCount == 0) {
                val nodesToInsert = mutableListOf<CourseNodeEntity>()
                var currentIndex = 0

                android.util.Log.d("DEBUG_REPO", "🌱 Створюємо структуру в базі для модуля: $lessonId")

                nodesToInsert.add(
                    CourseNodeEntity(
                        id = "${lessonId}_vocabulary",
                        lessonId = lessonId,
                        title = "Лексика",
                        type = "vocabulary",
                        orderIndex = currentIndex++,
                        isCompleted = false
                    )
                )

                nodesToInsert.add(
                    CourseNodeEntity(
                        id = "${lessonId}_grammar",
                        lessonId = lessonId,
                        title = "Граматика: ${lesson.grammar.topic}",
                        type = "grammar",
                        orderIndex = currentIndex++,
                        isCompleted = false
                    )
                )

                nodesToInsert.add(
                    CourseNodeEntity(
                        id = "${lessonId}_reading",
                        lessonId = lessonId,
                        title = "Читання",
                        type = "reading",
                        orderIndex = currentIndex++,
                        isCompleted = false
                    )
                )

                nodesToInsert.add(
                    CourseNodeEntity(
                        id = "${lessonId}_listening",
                        lessonId = lessonId,
                        title = "Аудіювання",
                        type = "listening",
                        orderIndex = currentIndex++,
                        isCompleted = false
                    )
                )

                if (lesson.examPractice.languageUse.isNotEmpty()) {
                    nodesToInsert.add(
                        CourseNodeEntity(
                            id = "${lessonId}_language_use",
                            lessonId = lessonId,
                            title = "Мовні конструкції",
                            type = "language_use",
                            orderIndex = currentIndex++,
                            isCompleted = false
                        )
                    )
                }

                nodesToInsert.add(
                    CourseNodeEntity(
                        id = "${lessonId}_writing",
                        lessonId = lessonId,
                        title = "Письмо",
                        type = "writing",
                        orderIndex = currentIndex++,
                        isCompleted = false
                    )
                )

                nodesToInsert.add(
                    CourseNodeEntity(
                        id = "${lessonId}_speaking",
                        lessonId = lessonId,
                        title = "Говоріння",
                        type = "speaking",
                        orderIndex = currentIndex++,
                        isCompleted = false
                    )
                )

                courseNodeDao.insertNodes(nodesToInsert)
                android.util.Log.d("DEBUG_REPO", "✅ Модуль $lessonId успішно додано до бази")
            } else {
                android.util.Log.d("DEBUG_REPO", "⏩ Модуль $lessonId вже існує в базі, пропускаємо")
            }
        }
    }
}
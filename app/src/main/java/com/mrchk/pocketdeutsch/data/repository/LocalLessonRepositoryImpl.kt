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
import okhttp3.Dispatcher
import javax.inject.Inject

class LocalLessonRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val courseNodeDao: CourseNodeDao
) : LessonRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getLessons(): List<Lesson> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("pocket_deutsch_module.json")
                .bufferedReader()
                .use { it.readText() }
            val dtoList = json.decodeFromString<List<ModuleResponse>>(jsonString)
            val lessons = dtoList.map { it.module.toDomainModel() }

            seedDatabaseIfNeeded(lessons)
            lessons

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

    override fun getLessonPathway(lessonId: String): Flow<List<CourseNode>> {
        return courseNodeDao.getNodesForLesson(lessonId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun completeNode(nodeId: String) {
        courseNodeDao.markNodeAsCompleted(nodeId)
    }

    override suspend fun getCompletedTasksCount(lessonId: String): Int {
        return courseNodeDao.getCompletedNodesCount(lessonId)
    }

    private suspend fun seedDatabaseIfNeeded(lessons: List<Lesson>) {
        val nodesCount = courseNodeDao.countAllNodes()

        if (nodesCount == 0) {
            val nodesToInsert = mutableListOf<CourseNodeEntity>()

            lessons.forEach { lesson ->
                var currentIndex = 0
                val lessonId = lesson.lessonId

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

                // 6. Письмо
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

                // 7. Говоріння
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
            }

            // Зберігаємо всі вузли в Room
            courseNodeDao.insertNodes(nodesToInsert)
        }
    }

}
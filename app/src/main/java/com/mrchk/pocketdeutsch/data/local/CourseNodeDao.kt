package com.mrchk.pocketdeutsch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseNodeDao {

    @Query("SELECT * FROM course_nodes WHERE lessonId = :lessonId ORDER BY orderIndex ASC")
    fun getNodesForLesson(lessonId: String): Flow<List<CourseNodeEntity>>

    @Query("UPDATE course_nodes SET isCompleted = 1 WHERE id = :nodeId")
    suspend fun markNodeAsCompleted(nodeId: String)

    @Query("SELECT COUNT(*) FROM course_nodes WHERE lessonId = :lessonId AND isCompleted = 1")
    suspend fun getCompletedNodesCount(lessonId: String): Int

    @Query("SELECT COUNT(*) FROM course_nodes")
    suspend fun countAllNodes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<CourseNodeEntity>)
}
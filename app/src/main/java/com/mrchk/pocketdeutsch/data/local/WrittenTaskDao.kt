package com.mrchk.pocketdeutsch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WrittenTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskResult(result: WrittenTaskResultEntity)

    @Query("SELECT * FROM written_task_result WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    fun getResultsForExercise(exerciseId: String): Flow<List<WrittenTaskResultEntity>>

    @Query("SELECT * FROM written_task_result WHERE pendingSync = 1")
    suspend fun getPendingSyncResults(): List<WrittenTaskResultEntity>
}
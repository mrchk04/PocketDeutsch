package com.mrchk.pocketdeutsch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        WrittenTaskResultEntity::class,
        CourseNodeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class PocketDeutschDatabase : RoomDatabase() {

    abstract fun writtenTaskDao(): WrittenTaskDao
    abstract fun courseNodeDao(): CourseNodeDao
}
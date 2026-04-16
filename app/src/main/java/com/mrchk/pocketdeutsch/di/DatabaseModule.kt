package com.mrchk.pocketdeutsch.di

import android.content.Context
import androidx.room.Room
import com.mrchk.pocketdeutsch.data.local.PocketDeutschDatabase
import com.mrchk.pocketdeutsch.data.local.WrittenTaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PocketDeutschDatabase {
        return Room.databaseBuilder(
            context,
            PocketDeutschDatabase::class.java,
            "pocket_deutsch_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWrittenTaskDao(database: PocketDeutschDatabase): WrittenTaskDao {
        return database.writtenTaskDao()
    }
}
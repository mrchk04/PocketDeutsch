package com.mrchk.pocketdeutsch.di

import com.mrchk.pocketdeutsch.data.repository.LocalLessonRepositoryImpl
import com.mrchk.pocketdeutsch.domain.repository.LessonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        impl: LocalLessonRepositoryImpl
    ): LessonRepository
}
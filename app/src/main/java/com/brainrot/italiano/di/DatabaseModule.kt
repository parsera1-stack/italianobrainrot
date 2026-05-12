package com.brainrot.italiano.di

import android.content.Context
import com.brainrot.italiano.data.local.AppDatabase
import com.brainrot.italiano.data.local.dao.DefaultWordDao
import com.brainrot.italiano.data.local.dao.WordAnswerHistoryDao
import com.brainrot.italiano.data.local.dao.WordDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao = database.wordDao()

    @Provides
    fun provideWordAnswerHistoryDao(database: AppDatabase): WordAnswerHistoryDao =
        database.wordAnswerHistoryDao()

    @Provides
    fun provideDefaultWordDao(database: AppDatabase): DefaultWordDao = database.defaultWordDao()
}

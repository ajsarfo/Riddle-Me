package com.sarftec.riddleme.injection

import android.content.Context
import androidx.room.Room
import com.sarftec.riddleme.database.RiddleDatabase
import com.sarftec.riddleme.repository.RiddleRepository
import com.sarftec.riddleme.repository.SettingsRepository
import com.sarftec.riddleme.repository.disk_impl.DiskSettingsRepoImpl
import com.sarftec.riddleme.repository.memory_impl.InMemoryRiddleRepositoryImpl
import com.sarftec.riddleme.repository.memory_impl.InMemorySettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    @Singleton
    @Provides
    fun riddleDatabase(@ApplicationContext context: Context): RiddleDatabase {
        return Room.databaseBuilder(
            context,
            RiddleDatabase::class.java,
            "riddle_database"
        ).build()
    }
}
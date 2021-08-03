package com.sarftec.riddleme.injection

import com.sarftec.riddleme.repository.RiddleRepository
import com.sarftec.riddleme.repository.SettingsRepository
import com.sarftec.riddleme.repository.disk_impl.DiskRiddleRepoImpl
import com.sarftec.riddleme.repository.disk_impl.DiskSettingsRepoImpl
import com.sarftec.riddleme.repository.memory_impl.InMemoryRiddleRepositoryImpl
import com.sarftec.riddleme.repository.memory_impl.InMemorySettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AbstractModule {

    @Singleton
    @Binds
    abstract fun riddleRepository(repository: DiskRiddleRepoImpl) : RiddleRepository

    @Singleton
    @Binds
    abstract fun settingsRepository(repository: DiskSettingsRepoImpl) : SettingsRepository
}
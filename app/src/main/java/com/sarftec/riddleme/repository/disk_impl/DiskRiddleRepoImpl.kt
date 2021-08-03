package com.sarftec.riddleme.repository.disk_impl

import android.content.Context
import com.sarftec.riddleme.database.RiddleDatabase
import com.sarftec.riddleme.model.Riddle
import com.sarftec.riddleme.repository.RiddleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DiskRiddleRepoImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: RiddleDatabase
) : RiddleRepository {

    override suspend fun riddles(): List<Riddle> {
        return database.riddleDao().riddles()
    }

    override suspend fun riddleForLevel(level: Int): Riddle? {
        return database.riddleDao().riddle(level)
    }

    override suspend fun riddlesForLevels(levels: List<Int>): List<Riddle> {
        return database.riddleDao().riddles(levels)
    }

    override suspend fun answered(answered: Boolean): List<Riddle> {
        return database.riddleDao().answered(answered)
    }

    override suspend fun update(id: Int, answered: Boolean) {
        return database.riddleDao().update(id, answered)
    }
}
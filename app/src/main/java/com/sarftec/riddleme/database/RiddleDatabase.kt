package com.sarftec.riddleme.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sarftec.riddleme.model.Riddle

@Database(entities = [Riddle::class], version = 1, exportSchema = false)
abstract class RiddleDatabase : RoomDatabase() {
    abstract fun riddleDao() : RiddleDao
}
package com.sarftec.riddleme.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sarftec.riddleme.model.Riddle

@Dao
interface RiddleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(riddles: List<Riddle>)

    @Query("select * from ${Riddle.RIDDLE_TABLE_NAME} where level = :level")
    suspend fun riddle(level: Int) : Riddle

    @Query("select * from ${Riddle.RIDDLE_TABLE_NAME} where level in (:levels)")
    suspend fun riddles(levels: List<Int>) : List<Riddle>

    @Query("select * from ${Riddle.RIDDLE_TABLE_NAME}")
    suspend fun riddles() : List<Riddle>

    @Query("select * from ${Riddle.RIDDLE_TABLE_NAME} where answered = :answered")
    suspend fun answered(answered: Boolean) : List<Riddle>

    @Query("update ${Riddle.RIDDLE_TABLE_NAME} set answered = :answered where id = :id")
    suspend fun update(id: Int, answered: Boolean)
}
package com.sarftec.riddleme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarftec.riddleme.model.Riddle.Companion.RIDDLE_TABLE_NAME

@Entity(tableName = RIDDLE_TABLE_NAME)
class Riddle(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val level: Int,
    val question: String,
    val answer: String,
    var answered: Boolean = false
) {
    companion object {
        const val RIDDLE_TABLE_NAME = "riddle_table"
    }
}
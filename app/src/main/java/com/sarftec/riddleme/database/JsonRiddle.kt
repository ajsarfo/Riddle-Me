package com.sarftec.riddleme.database

import com.sarftec.riddleme.model.Riddle
import kotlinx.serialization.Serializable

@Serializable
class JsonRiddle(
    val level: String,
    val question: String,
    val answer: String
) {
    fun toRiddle() : Riddle {
        return Riddle(
            level = level.toInt(),
            question = question,
            answer =  answer
        )
    }
}
package com.sarftec.riddleme.repository.memory_impl

import android.content.Context
import com.sarftec.riddleme.model.Riddle
import com.sarftec.riddleme.repository.RiddleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class InMemoryRiddleRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context
) : RiddleRepository {

    private val riddles = listOf(
        Riddle(
            level = 1,
            question = "What goes up and never comes down?",
            answer = "smoke"
        ),
        Riddle(
            level = 2,
            question = "I am the first program a beginner programmer writes. What am I?",
            answer = "hello"
        ),
        Riddle(
            level = 3,
            question = "What is the root of all evil?",
            answer = "Money"
        ),
        Riddle(
            level = 4,
            question = "I don't know where I stand in life. Who am I?",
            answer = "junior"
        )
    )

    override suspend fun riddles(): List<Riddle> {
        return riddles
    }

    override suspend fun riddleForLevel(level: Int): Riddle? {
       return riddles.firstOrNull {
            it.level == level
        }
    }

    override suspend fun riddlesForLevels(levels: List<Int>): List<Riddle> {
        return riddles.filter { levels.contains(it.level) }
    }

    override suspend fun answered(answered: Boolean): List<Riddle> {
        return riddles.filter { it.answered }
    }

    override suspend fun update(id: Int, answered: Boolean) {
        riddles.firstOrNull {
            it.id == id
        }?.let {
            it.answered = answered
        }
    }
}
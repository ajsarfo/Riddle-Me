package com.sarftec.riddleme.repository

import com.sarftec.riddleme.model.Riddle

interface RiddleRepository {
   suspend fun riddles() : List<Riddle>
   suspend fun riddleForLevel(level: Int) : Riddle?
   suspend fun riddlesForLevels(levels: List<Int>) : List<Riddle>
   suspend fun answered(answered: Boolean) : List<Riddle>
   suspend fun update(id: Int, answered: Boolean)
}
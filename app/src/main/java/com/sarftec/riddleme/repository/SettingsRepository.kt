package com.sarftec.riddleme.repository

interface SettingsRepository {

    suspend fun levelSize() : Int
    suspend fun setLevelSize(size: Int)
    suspend fun saveLevel(level: Int)
    suspend fun getLevel() : Int

    suspend fun saveCoins(amount: Int)
    suspend fun getCoins() : Int

    suspend fun isNewApp() : Boolean
    suspend fun markAppNew(new: Boolean)

    suspend fun setSoundOn(isSound: Boolean)
    suspend fun isSoundOn() : Boolean

    suspend fun setSkipCount(count: Int)
    suspend fun getSkipCount() : Int

    suspend fun setMonthDayInfo(monthDayInfo: MonthDayInfo)
    suspend fun getMonthDayInfo() : MonthDayInfo

    class MonthDayInfo(
        val month: Int,
        val day: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if(other !is MonthDayInfo) return false
            return other.day == day && other.month == month
        }

        override fun hashCode(): Int {
            var result = month
            result = 31 * result + day
            return result
        }
    }
}
package com.sarftec.riddleme.repository.memory_impl

import android.content.Context
import com.sarftec.riddleme.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class InMemorySettingsRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context
) : SettingsRepository {

    private var currentLevel = 1

    private var currentCoins = 100000

    private var isNew = true

    private var isSoundOn = true

    private var skipLevelCount = 3

    private var weekDayInfo = SettingsRepository.MonthDayInfo(
        0,
        0
    )

    override suspend fun levelSize(): Int {
        return 4
    }

    override suspend fun setLevelSize(size: Int) {
        //Do nothing
    }

    override suspend fun saveLevel(level: Int) {
        currentLevel =  level
    }

    override suspend fun getLevel(): Int {
       return currentLevel
    }

    override suspend fun saveCoins(amount: Int) {
        currentCoins = amount
    }

    override suspend fun getCoins(): Int {
        return currentCoins
    }

    override suspend fun isNewApp(): Boolean {
        return isNew
    }

    override suspend fun markAppNew(new: Boolean) {
        isNew = new
    }

    override suspend fun setSoundOn(isSoundOn: Boolean) {
        this.isSoundOn = isSoundOn
    }

    override suspend fun isSoundOn(): Boolean {
        return isSoundOn
    }

    override suspend fun setSkipCount(count: Int) {
        skipLevelCount = count
    }

    override suspend fun getSkipCount(): Int {
        return skipLevelCount
    }

    override suspend fun setMonthDayInfo(monthDayInfo: SettingsRepository.MonthDayInfo) {
        this.weekDayInfo = monthDayInfo
    }

    override suspend fun getMonthDayInfo(): SettingsRepository.MonthDayInfo {
        return weekDayInfo
    }
}
package com.sarftec.riddleme.repository.disk_impl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.sarftec.riddleme.repository.SettingsRepository
import com.sarftec.riddleme.tools.editSettings
import com.sarftec.riddleme.tools.readSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DiskSettingsRepoImpl @Inject constructor(
    @ApplicationContext val context: Context
) : SettingsRepository {

    private var monthDayInfo: SettingsRepository.MonthDayInfo? = null

    override suspend fun levelSize(): Int {
        return context.readSettings(levels, 0).first()
    }

    override suspend fun setLevelSize(size: Int) {
        context.editSettings(levels, size)
    }

    override suspend fun saveLevel(level: Int) {
        context.editSettings(stageLevel, level)
    }

    override suspend fun getLevel(): Int {
        return context.readSettings(stageLevel, 1).first()
    }

    override suspend fun saveCoins(amount: Int) {
        context.editSettings(coins, amount)
    }

    override suspend fun getCoins(): Int {
        return context.readSettings(coins, 400).first()
    }

    override suspend fun isNewApp(): Boolean {
        return context.readSettings(newApp, true).first()
    }

    override suspend fun markAppNew(new: Boolean) {
        context.editSettings(newApp, new)
    }

    override suspend fun setSoundOn(isSound: Boolean) {
        context.editSettings(sound, isSound)
    }

    override suspend fun isSoundOn(): Boolean {
        return context.readSettings(sound,true).first()
    }

    override suspend fun setSkipCount(count: Int) {
        context.editSettings(skipCount, count)
    }

    override suspend fun getSkipCount(): Int {
        return context.readSettings(skipCount, 3).first()
    }

    override suspend fun setMonthDayInfo(monthDayInfo: SettingsRepository.MonthDayInfo) {
        this.monthDayInfo = monthDayInfo
        context.editSettings(month, monthDayInfo.month)
        context.editSettings(day, monthDayInfo.day)
    }

    override suspend fun getMonthDayInfo(): SettingsRepository.MonthDayInfo {
        return monthDayInfo ?: SettingsRepository.MonthDayInfo(
            context.readSettings(month, 0).first(),
            context.readSettings(day, 0).first()
        ).also {
           monthDayInfo = it
        }
    }

    companion object {
        val levels = intPreferencesKey("levels")
        val stageLevel = intPreferencesKey("level")
        val coins = intPreferencesKey("coins")
        val skipCount = intPreferencesKey("skip_count")
        val newApp = booleanPreferencesKey("new_app")
        val sound = booleanPreferencesKey("sound")
        val day = intPreferencesKey("day")
        val month = intPreferencesKey("month")
    }
}
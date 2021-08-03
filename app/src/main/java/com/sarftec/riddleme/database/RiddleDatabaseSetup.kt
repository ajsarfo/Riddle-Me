package com.sarftec.riddleme.database

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.sarftec.riddleme.repository.SettingsRepository
import com.sarftec.riddleme.tools.editSettings
import com.sarftec.riddleme.tools.readSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RiddleDatabaseSetup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val riddleDatabase: RiddleDatabase,
    private val settingsRepository: SettingsRepository
) {

    suspend fun setupDatabase() {
        if(context.readSettings(isPrepared, false).first()) return
        Log.v("TAG", "Preparing database")
        context.assets.open("riddles/riddles.json")
            .bufferedReader()
            .use { reader ->
                val riddles: List<JsonRiddle> = Json.decodeFromString(reader.readText())
                riddleDatabase.riddleDao().insert(
                    riddles.map { it.toRiddle() }
                )
                settingsRepository.setLevelSize(riddles.size)
            }
        context.editSettings(isPrepared, true)
    }

    companion object {
        val isPrepared = booleanPreferencesKey("is_prepared")
    }
}
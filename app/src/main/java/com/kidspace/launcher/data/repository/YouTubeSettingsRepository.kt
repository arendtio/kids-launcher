package com.kidspace.launcher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kidspace.launcher.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.youtubeSettingsDataStore: DataStore<Preferences> by preferencesDataStore("youtube_settings")

class YouTubeSettingsRepository(private val context: Context) {
    private val dataStore = context.youtubeSettingsDataStore

    fun observeApiKey(): Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.API_KEY].orEmpty()
    }

    suspend fun saveApiKey(apiKey: String) {
        dataStore.edit { prefs ->
            if (apiKey.isBlank()) {
                prefs.remove(Keys.API_KEY)
            } else {
                prefs[Keys.API_KEY] = apiKey.trim()
            }
        }
    }

    suspend fun effectiveApiKey(): String {
        val saved = observeApiKey().first()
        return saved.ifBlank { BuildConfig.YOUTUBE_API_KEY }
    }

    private object Keys {
        val API_KEY = stringPreferencesKey("youtube_api_key")
    }
}

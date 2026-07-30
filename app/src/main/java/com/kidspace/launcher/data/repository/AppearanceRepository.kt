package com.kidspace.launcher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kidspace.launcher.data.model.AppearanceDefaults
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundPresets
import com.kidspace.launcher.data.model.BackgroundType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appearanceDataStore: DataStore<Preferences> by preferencesDataStore("appearance")

class AppearanceRepository(private val context: Context) {
    private val dataStore = context.appearanceDataStore

    fun observeSettings(): Flow<AppearanceSettings> = dataStore.data.map { prefs ->
        AppearanceSettings(
            backgroundType = prefs[Keys.BACKGROUND_TYPE]?.let { BackgroundType.valueOf(it) }
                ?: BackgroundType.PRESET,
            backgroundPreset = prefs[Keys.BACKGROUND_PRESET] ?: AppearanceDefaults.BACKGROUND_PRESET,
            customBackgroundUri = prefs[Keys.CUSTOM_BACKGROUND_URI],
            primaryColor = prefs[Keys.PRIMARY_COLOR] ?: AppearanceDefaults.PRIMARY,
            secondaryColor = prefs[Keys.SECONDARY_COLOR] ?: AppearanceDefaults.SECONDARY,
            accentColor = prefs[Keys.ACCENT_COLOR] ?: AppearanceDefaults.ACCENT,
        )
    }

    suspend fun saveSettings(settings: AppearanceSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_TYPE] = settings.backgroundType.name
            prefs[Keys.BACKGROUND_PRESET] = settings.backgroundPreset
            if (settings.customBackgroundUri != null) {
                prefs[Keys.CUSTOM_BACKGROUND_URI] = settings.customBackgroundUri
            } else {
                prefs.remove(Keys.CUSTOM_BACKGROUND_URI)
            }
            prefs[Keys.PRIMARY_COLOR] = settings.primaryColor
            prefs[Keys.SECONDARY_COLOR] = settings.secondaryColor
            prefs[Keys.ACCENT_COLOR] = settings.accentColor
        }
    }

    private object Keys {
        val BACKGROUND_TYPE = stringPreferencesKey("background_type")
        val BACKGROUND_PRESET = stringPreferencesKey("background_preset")
        val CUSTOM_BACKGROUND_URI = stringPreferencesKey("custom_background_uri")
        val PRIMARY_COLOR = longPreferencesKey("primary_color")
        val SECONDARY_COLOR = longPreferencesKey("secondary_color")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
    }
}

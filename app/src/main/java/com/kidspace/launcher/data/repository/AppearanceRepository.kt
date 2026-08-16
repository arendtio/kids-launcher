package com.kidspace.launcher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kidspace.launcher.data.model.AppearanceDefaults
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundPresets
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.util.BackgroundImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import android.net.Uri

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
            backgroundAnimationsEnabled = prefs[Keys.BACKGROUND_ANIMATIONS_ENABLED] ?: true,
        )
    }

    suspend fun saveSettings(settings: AppearanceSettings) {
        val normalized = if (settings.backgroundType == BackgroundType.PRESET) {
            BackgroundImageStorage.clear(context)
            settings.copy(customBackgroundUri = null)
        } else {
            settings
        }
        dataStore.edit { prefs ->
            prefs[Keys.BACKGROUND_TYPE] = normalized.backgroundType.name
            prefs[Keys.BACKGROUND_PRESET] = normalized.backgroundPreset
            if (normalized.customBackgroundUri != null) {
                prefs[Keys.CUSTOM_BACKGROUND_URI] = normalized.customBackgroundUri
            } else {
                prefs.remove(Keys.CUSTOM_BACKGROUND_URI)
            }
            prefs[Keys.PRIMARY_COLOR] = normalized.primaryColor
            prefs[Keys.SECONDARY_COLOR] = normalized.secondaryColor
            prefs[Keys.ACCENT_COLOR] = normalized.accentColor
            prefs[Keys.BACKGROUND_ANIMATIONS_ENABLED] = normalized.backgroundAnimationsEnabled
        }
    }

    suspend fun importCustomBackground(sourceUri: Uri): AppearanceSettings {
        val savedPath = BackgroundImageStorage.saveFromUri(context, sourceUri)
        val updated = observeSettings().first().copy(
            backgroundType = BackgroundType.CUSTOM,
            customBackgroundUri = savedPath,
        )
        saveSettings(updated)
        return updated
    }

    suspend fun clearCustomBackground(): AppearanceSettings {
        BackgroundImageStorage.clear(context)
        val updated = observeSettings().first().copy(
            backgroundType = BackgroundType.PRESET,
            customBackgroundUri = null,
        )
        saveSettings(updated)
        return updated
    }

    private object Keys {
        val BACKGROUND_TYPE = stringPreferencesKey("background_type")
        val BACKGROUND_PRESET = stringPreferencesKey("background_preset")
        val CUSTOM_BACKGROUND_URI = stringPreferencesKey("custom_background_uri")
        val PRIMARY_COLOR = longPreferencesKey("primary_color")
        val SECONDARY_COLOR = longPreferencesKey("secondary_color")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val BACKGROUND_ANIMATIONS_ENABLED = booleanPreferencesKey("background_animations_enabled")
    }
}

package com.kidspace.launcher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.kidspace.launcher.data.model.ParentSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.parentSettingsDataStore: DataStore<Preferences> by preferencesDataStore("parent_settings")

class ParentSettingsRepository(private val context: Context) {
    private val dataStore = context.parentSettingsDataStore

    fun observeSettings(): Flow<ParentSettings> = dataStore.data.map { prefs ->
        ParentSettings(
            webViewUploadDebugEnabled = prefs[Keys.WEBVIEW_UPLOAD_DEBUG] ?: false,
        )
    }

    suspend fun setWebViewUploadDebugEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.WEBVIEW_UPLOAD_DEBUG] = enabled
        }
    }

    private object Keys {
        val WEBVIEW_UPLOAD_DEBUG = booleanPreferencesKey("webview_upload_debug")
    }
}

package com.kidspace.launcher.shortcuts

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kidspace.launcher.data.model.InstalledApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.legacyShortcutsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "legacy_shortcuts",
)

data class LegacyShortcutEntry(
    val id: String,
    val label: String,
    val intentUri: String,
) {
    fun toInstalledApp(): InstalledApp = InstalledApp(
        label = label,
        packageName = id,
        shortcutHostPackage = null,
        shortcutId = null,
        browserLabel = "Pinned shortcut",
        legacyIntentUri = intentUri,
    )
}

class LegacyShortcutStore(private val context: Context) {
    private val dataStore = context.legacyShortcutsDataStore
    private val entriesKey = stringPreferencesKey("entries_json")

    suspend fun loadAll(): List<LegacyShortcutEntry> {
        val json = dataStore.data.map { prefs -> prefs[entriesKey].orEmpty() }.first()
        if (json.isBlank()) {
            cachedEntries = emptyList()
            return emptyList()
        }
        cachedEntries = decode(json)
        return cachedEntries
    }

    suspend fun add(label: String, intentUri: String): LegacyShortcutEntry {
        val entry = LegacyShortcutEntry(
            id = UUID.randomUUID().toString(),
            label = label,
            intentUri = intentUri,
        )
        dataStore.edit { prefs ->
            val current = decode(prefs[entriesKey].orEmpty())
            cachedEntries = current + entry
            prefs[entriesKey] = encode(cachedEntries)
        }
        return entry
    }

    fun intentUriFor(legacyId: String): String? =
        cachedEntries.find { it.id == legacyId }?.intentUri

    private var cachedEntries: List<LegacyShortcutEntry> = emptyList()

    private fun decode(json: String): List<LegacyShortcutEntry> {
        if (json.isBlank()) return emptyList()
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    LegacyShortcutEntry(
                        id = item.getString("id"),
                        label = item.getString("label"),
                        intentUri = item.getString("intentUri"),
                    ),
                )
            }
        }
    }

    private fun encode(entries: List<LegacyShortcutEntry>): String {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("label", entry.label)
                    .put("intentUri", entry.intentUri),
            )
        }
        return array.toString()
    }
}

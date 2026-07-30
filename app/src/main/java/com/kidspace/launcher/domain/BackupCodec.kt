package com.kidspace.launcher.domain

import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.TileType
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

data class KidSpaceBackup(
    val version: Int = CURRENT_VERSION,
    val exportedAt: String = Instant.now().toString(),
    val tiles: List<ChildTile>,
    val appearance: AppearanceSettings,
    val customBackgroundBase64: String? = null,
    val customBackgroundMimeType: String? = null,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

object BackupCodec {
    fun encode(backup: KidSpaceBackup): String {
        val root = JSONObject()
        root.put("version", backup.version)
        root.put("exportedAt", backup.exportedAt)

        val tilesArray = JSONArray()
        backup.tiles.sortedBy { it.sortOrder }.forEach { tile ->
            tilesArray.put(
                JSONObject()
                    .put("type", tile.type.name)
                    .put("label", tile.label)
                    .put("target", tile.target)
                    .put("iconKey", tile.iconKey)
                    .put("sortOrder", tile.sortOrder),
            )
        }
        root.put("tiles", tilesArray)

        val appearance = JSONObject()
            .put("backgroundType", backup.appearance.backgroundType.name)
            .put("backgroundPreset", backup.appearance.backgroundPreset)
            .put("primaryColor", colorToHex(backup.appearance.primaryColor))
            .put("secondaryColor", colorToHex(backup.appearance.secondaryColor))
            .put("accentColor", colorToHex(backup.appearance.accentColor))

        if (backup.customBackgroundBase64 != null && backup.customBackgroundMimeType != null) {
            appearance.put(
                "customBackground",
                JSONObject()
                    .put("mimeType", backup.customBackgroundMimeType)
                    .put("dataBase64", backup.customBackgroundBase64),
            )
        }
        root.put("appearance", appearance)

        return root.toString(2)
    }

    fun decode(json: String): KidSpaceBackup {
        val root = JSONObject(json)
        val version = root.getInt("version")
        require(version == KidSpaceBackup.CURRENT_VERSION) {
            "Unsupported backup version: $version"
        }

        val tiles = buildList {
            val array = root.getJSONArray("tiles")
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    ChildTile(
                        type = TileType.valueOf(item.getString("type")),
                        label = item.getString("label"),
                        target = item.getString("target"),
                        iconKey = item.getString("iconKey"),
                        sortOrder = item.getInt("sortOrder"),
                    ),
                )
            }
        }

        val appearanceJson = root.getJSONObject("appearance")
        val customBackground = appearanceJson.optJSONObject("customBackground")
        val appearance = AppearanceSettings(
            backgroundType = BackgroundType.valueOf(appearanceJson.getString("backgroundType")),
            backgroundPreset = appearanceJson.getString("backgroundPreset"),
            customBackgroundUri = null,
            primaryColor = hexToColor(appearanceJson.getString("primaryColor")),
            secondaryColor = hexToColor(appearanceJson.getString("secondaryColor")),
            accentColor = hexToColor(appearanceJson.getString("accentColor")),
        )

        return KidSpaceBackup(
            version = version,
            exportedAt = root.optString("exportedAt", ""),
            tiles = tiles,
            appearance = appearance,
            customBackgroundBase64 = customBackground?.optString("dataBase64")?.takeIf { it.isNotBlank() },
            customBackgroundMimeType = customBackground?.optString("mimeType")?.takeIf { it.isNotBlank() },
        )
    }

    private fun colorToHex(color: Long): String = String.format("#%08X", color)

    private fun hexToColor(hex: String): Long {
        val normalized = hex.removePrefix("#")
        return normalized.toLong(16)
    }
}

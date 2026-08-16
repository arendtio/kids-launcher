package com.kidspace.launcher.domain

import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.data.model.WebLaunchMode
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
        const val CURRENT_VERSION = 2
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
                    .put("sortOrder", tile.sortOrder)
                    .put("webLaunchMode", tile.webLaunchMode.name)
                    .put("cameraPolicy", tile.cameraPolicy.name)
                    .put("microphonePolicy", tile.microphonePolicy.name)
                    .put("locationPolicy", tile.locationPolicy.name)
                    .put("fileUploadPolicy", tile.fileUploadPolicy.name)
                    .put("downloadPolicy", tile.downloadPolicy.name)
                    .put("fullscreenPolicy", tile.fullscreenPolicy.name)
                    .put("cameraCapturePolicy", tile.cameraCapturePolicy.name),
            )
        }
        root.put("tiles", tilesArray)

        val appearance = JSONObject()
            .put("backgroundType", backup.appearance.backgroundType.name)
            .put("backgroundPreset", backup.appearance.backgroundPreset)
            .put("primaryColor", colorToHex(backup.appearance.primaryColor))
            .put("secondaryColor", colorToHex(backup.appearance.secondaryColor))
            .put("accentColor", colorToHex(backup.appearance.accentColor))
            .put("backgroundAnimationsEnabled", backup.appearance.backgroundAnimationsEnabled)

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
        require(version in 1..KidSpaceBackup.CURRENT_VERSION) {
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
                        webLaunchMode = item.optString("webLaunchMode", WebLaunchMode.EXTERNAL.name)
                            .let(WebLaunchMode::valueOf),
                        cameraPolicy = item.optString("cameraPolicy", PermissionPolicy.GRANT.name)
                            .let(PermissionPolicy::valueOf),
                        microphonePolicy = item.optString("microphonePolicy", PermissionPolicy.GRANT.name)
                            .let(PermissionPolicy::valueOf),
                        locationPolicy = item.optString("locationPolicy", PermissionPolicy.GRANT.name)
                            .let(PermissionPolicy::valueOf),
                        fileUploadPolicy = item.optString("fileUploadPolicy", PermissionPolicy.DENY.name)
                            .let(PermissionPolicy::valueOf),
                        downloadPolicy = item.optString("downloadPolicy", PermissionPolicy.DENY.name)
                            .let(PermissionPolicy::valueOf),
                        fullscreenPolicy = item.optString("fullscreenPolicy", PermissionPolicy.GRANT.name)
                            .let(PermissionPolicy::valueOf),
                        cameraCapturePolicy = item.optString("cameraCapturePolicy", PermissionPolicy.DENY.name)
                            .let(PermissionPolicy::valueOf),
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
            backgroundAnimationsEnabled = appearanceJson.optBoolean("backgroundAnimationsEnabled", true),
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

package com.kidspace.launcher.domain

import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.TileType
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupCodecTest {
    @Test
    fun encodeAndDecode_roundTripsBackup() {
        val backup = KidSpaceBackup(
            tiles = listOf(
                ChildTile(
                    type = TileType.APP,
                    label = "YouTube Kids",
                    target = "com.google.android.apps.youtube.kids",
                    iconKey = "app:com.google.android.apps.youtube.kids",
                    sortOrder = 0,
                ),
                ChildTile(
                    type = TileType.WEBSITE,
                    label = "PBS",
                    target = "https://pbskids.org",
                    iconKey = "favicon:pbskids.org",
                    sortOrder = 1,
                ),
            ),
            appearance = AppearanceSettings(
                backgroundType = BackgroundType.PRESET,
                backgroundPreset = "sky_meadow",
                primaryColor = 0xFF4F9AD8,
                secondaryColor = 0xFF7EC8E3,
                accentColor = 0xFFFFAB76,
            ),
            customBackgroundBase64 = null,
            customBackgroundMimeType = null,
        )

        val decoded = BackupCodec.decode(BackupCodec.encode(backup))

        assertEquals(2, decoded.tiles.size)
        assertEquals("YouTube Kids", decoded.tiles[0].label)
        assertEquals(TileType.WEBSITE, decoded.tiles[1].type)
        assertEquals(0xFF4F9AD8, decoded.appearance.primaryColor)
    }

    @Test
    fun decode_includesCustomBackgroundPayload() {
        val json = """
            {
              "version": 1,
              "exportedAt": "2026-07-30T10:00:00Z",
              "tiles": [],
              "appearance": {
                "backgroundType": "CUSTOM",
                "backgroundPreset": "sky_meadow",
                "primaryColor": "#FF4F9AD8",
                "secondaryColor": "#FF7EC8E3",
                "accentColor": "#FFFFAB76",
                "customBackground": {
                  "mimeType": "image/png",
                  "dataBase64": "aGVsbG8="
                }
              }
            }
        """.trimIndent()

        val decoded = BackupCodec.decode(json)

        assertEquals(BackgroundType.CUSTOM, decoded.appearance.backgroundType)
        assertEquals("aGVsbG8=", decoded.customBackgroundBase64)
        assertEquals("image/png", decoded.customBackgroundMimeType)
    }
}

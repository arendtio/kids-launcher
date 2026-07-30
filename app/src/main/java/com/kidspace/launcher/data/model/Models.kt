package com.kidspace.launcher.data.model

enum class TileType {
    APP,
    WEBSITE,
    YOUTUBE,
}

enum class BackgroundType {
    PRESET,
    CUSTOM,
}

data class ChildTile(
    val id: Long = 0,
    val type: TileType,
    val label: String,
    val target: String,
    val iconKey: String,
    val sortOrder: Int,
)

data class InstalledApp(
    val packageName: String,
    val label: String,
)

data class AppearanceSettings(
    val backgroundType: BackgroundType = BackgroundType.PRESET,
    val backgroundPreset: String = BackgroundPresets.SUNNY_MEADOW,
    val customBackgroundUri: String? = null,
    val primaryColor: Long = 0xFF6B9DFF,
    val secondaryColor: Long = 0xFFFFD93D,
    val accentColor: Long = 0xFFFF6B9D,
)

object BackgroundPresets {
    const val SUNNY_MEADOW = "sunny_meadow"
    const val OCEAN_BUBBLES = "ocean_bubbles"
    const val CANDY_CLOUDS = "candy_clouds"
    const val STARRY_NIGHT = "starry_night"
    const val RAINBOW_HILLS = "rainbow_hills"

    val all = listOf(
        SUNNY_MEADOW,
        OCEAN_BUBBLES,
        CANDY_CLOUDS,
        STARRY_NIGHT,
        RAINBOW_HILLS,
    )

    val displayNames = mapOf(
        SUNNY_MEADOW to "Sunny Meadow",
        OCEAN_BUBBLES to "Ocean Bubbles",
        CANDY_CLOUDS to "Candy Clouds",
        STARRY_NIGHT to "Starry Night",
        RAINBOW_HILLS to "Rainbow Hills",
    )
}

object ColorPresets {
    data class Theme(val name: String, val primary: Long, val secondary: Long, val accent: Long)

    val themes = listOf(
        Theme("Sky Play", 0xFF6B9DFF, 0xFFFFD93D, 0xFFFF6B9D),
        Theme("Forest Fun", 0xFF4CAF50, 0xFFFFEB3B, 0xFFFF9800),
        Theme("Berry Blast", 0xFF9C27B0, 0xFFE91E63, 0xFFFF5722),
        Theme("Ocean Dream", 0xFF03A9F4, 0xFF00BCD4, 0xFF4DD0E1),
        Theme("Sunset Joy", 0xFFFF7043, 0xFFFFCA28, 0xFFAB47BC),
    )
}

object RandomIcons {
    val icons = listOf(
        "star", "heart", "rocket", "balloon", "rainbow",
        "flower", "moon", "sun", "fish", "butterfly",
    )
}

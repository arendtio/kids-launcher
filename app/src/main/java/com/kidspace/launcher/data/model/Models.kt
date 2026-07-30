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

enum class WebLaunchMode {
    EXTERNAL,
    IN_APP,
}

enum class PermissionPolicy {
    GRANT,
    DENY,
}

data class ChildTile(
    val id: Long = 0,
    val type: TileType,
    val label: String,
    val target: String,
    val iconKey: String,
    val sortOrder: Int,
    val webLaunchMode: WebLaunchMode = WebLaunchMode.EXTERNAL,
    val cameraPolicy: PermissionPolicy = PermissionPolicy.GRANT,
    val microphonePolicy: PermissionPolicy = PermissionPolicy.GRANT,
    val locationPolicy: PermissionPolicy = PermissionPolicy.GRANT,
)

data class WebLinkConfig(
    val webLaunchMode: WebLaunchMode = WebLaunchMode.IN_APP,
    val cameraPolicy: PermissionPolicy = PermissionPolicy.GRANT,
    val microphonePolicy: PermissionPolicy = PermissionPolicy.GRANT,
    val locationPolicy: PermissionPolicy = PermissionPolicy.DENY,
)

data class InstalledApp(
    val packageName: String,
    val label: String,
)

data class AppearanceSettings(
    val backgroundType: BackgroundType = BackgroundType.PRESET,
    val backgroundPreset: String = AppearanceDefaults.BACKGROUND_PRESET,
    val customBackgroundUri: String? = null,
    val primaryColor: Long = AppearanceDefaults.PRIMARY,
    val secondaryColor: Long = AppearanceDefaults.SECONDARY,
    val accentColor: Long = AppearanceDefaults.ACCENT,
)

/** Cohesive default palette: soft sky blues with a warm peach accent. */
object AppearanceDefaults {
    const val BACKGROUND_PRESET = BackgroundPresets.SKY_MEADOW
    const val PRIMARY = 0xFF4F9AD8L
    const val SECONDARY = 0xFF7EC8E3L
    const val ACCENT = 0xFFFFAB76L
}

object BackgroundPresets {
    const val SKY_MEADOW = "sky_meadow"
    const val OCEAN_BUBBLES = "ocean_bubbles"
    const val CANDY_CLOUDS = "candy_clouds"
    const val STARRY_NIGHT = "starry_night"
    const val RAINBOW_HILLS = "rainbow_hills"

    // Legacy alias kept for stored preferences
    const val SUNNY_MEADOW = SKY_MEADOW

    val all = listOf(
        SKY_MEADOW,
        OCEAN_BUBBLES,
        CANDY_CLOUDS,
        STARRY_NIGHT,
        RAINBOW_HILLS,
    )

    val displayNames = mapOf(
        SKY_MEADOW to "Sky Meadow",
        OCEAN_BUBBLES to "Ocean Bubbles",
        CANDY_CLOUDS to "Candy Clouds",
        STARRY_NIGHT to "Starry Night",
        RAINBOW_HILLS to "Rainbow Hills",
    )
}

object ColorPresets {
    data class Theme(val name: String, val primary: Long, val secondary: Long, val accent: Long)

    val themes = listOf(
        Theme("Sky Meadow", 0xFF4F9AD8, 0xFF7EC8E3, 0xFFFFAB76),
        Theme("Forest Fun", 0xFF43A047, 0xFF81C784, 0xFFFFCA28),
        Theme("Berry Bliss", 0xFF8E6BBE, 0xFFC9A0DC, 0xFFFF8FAB),
        Theme("Ocean Dream", 0xFF0288D1, 0xFF4FC3F7, 0xFF80DEEA),
        Theme("Sunset Glow", 0xFFE57373, 0xFFFFB74D, 0xFF9575CD),
    )
}

object RandomIcons {
    val icons = listOf(
        "star", "heart", "rocket", "balloon", "rainbow",
        "flower", "moon", "sun", "fish", "butterfly",
    )
}

package com.kidspace.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundPresets
import com.kidspace.launcher.data.model.BackgroundType

fun Long.toComposeColor(): Color = Color(this)

fun presetGradient(presetId: String): Brush = when (presetId) {
    BackgroundPresets.OCEAN_BUBBLES -> Brush.verticalGradient(
        listOf(Color(0xFF4FC3F7), Color(0xFF00838F)),
    )
    BackgroundPresets.CANDY_CLOUDS -> Brush.verticalGradient(
        listOf(Color(0xFFF48FB1), Color(0xFFCE93D8)),
    )
    BackgroundPresets.STARRY_NIGHT -> Brush.verticalGradient(
        listOf(Color(0xFF1A237E), Color(0xFF4527A0)),
    )
    BackgroundPresets.RAINBOW_HILLS -> Brush.verticalGradient(
        listOf(Color(0xFFFF8A65), Color(0xFFFFD54F), Color(0xFF81C784)),
    )
    else -> Brush.verticalGradient(
        listOf(Color(0xFFFFF176), Color(0xFFAED581)),
    )
}

fun backgroundBrush(settings: AppearanceSettings): Brush {
    return when (settings.backgroundType) {
        BackgroundType.PRESET -> presetGradient(settings.backgroundPreset)
        BackgroundType.CUSTOM -> presetGradient(settings.backgroundPreset)
    }
}

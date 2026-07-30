package com.kidspace.launcher.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundPresets
import com.kidspace.launcher.data.model.BackgroundType

fun Long.toComposeColor(): Color = Color(this)

fun presetGradient(presetId: String): Brush = when (presetId) {
    BackgroundPresets.SKY_MEADOW, "sunny_meadow" -> Brush.verticalGradient(
        listOf(Color(0xFFB8E4FF), Color(0xFFC8F0D4)),
    )
    BackgroundPresets.OCEAN_BUBBLES -> Brush.verticalGradient(
        listOf(Color(0xFF81D4FA), Color(0xFF4DB6AC)),
    )
    BackgroundPresets.CANDY_CLOUDS -> Brush.verticalGradient(
        listOf(Color(0xFFF8BBD9), Color(0xFFD1C4E9)),
    )
    BackgroundPresets.STARRY_NIGHT -> Brush.verticalGradient(
        listOf(Color(0xFF283593), Color(0xFF5E35B1)),
    )
    BackgroundPresets.RAINBOW_HILLS -> Brush.verticalGradient(
        listOf(Color(0xFFFFAB91), Color(0xFFFFE082), Color(0xFFA5D6A7)),
    )
    else -> Brush.verticalGradient(
        listOf(Color(0xFFB8E4FF), Color(0xFFC8F0D4)),
    )
}

fun backgroundBrush(settings: AppearanceSettings): Brush {
    return when (settings.backgroundType) {
        BackgroundType.PRESET -> presetGradient(settings.backgroundPreset)
        BackgroundType.CUSTOM -> presetGradient(settings.backgroundPreset)
    }
}

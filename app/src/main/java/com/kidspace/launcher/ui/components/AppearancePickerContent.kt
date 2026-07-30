package com.kidspace.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundPresets
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.data.model.ColorPresets
import com.kidspace.launcher.ui.theme.toComposeColor

@Composable
fun AppearancePickerContent(
    draft: AppearanceSettings,
    onDraftChange: (AppearanceSettings) -> Unit,
    showPhotoSection: Boolean,
    modifier: Modifier = Modifier,
    photoSection: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showPhotoSection && photoSection != null) {
            photoSection()
        }

        Text("Backgrounds", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BackgroundPresets.all.forEach { presetId ->
                val selected = draft.backgroundPreset == presetId &&
                    draft.backgroundType == BackgroundType.PRESET
                PresetBackgroundCard(
                    presetId = presetId,
                    label = BackgroundPresets.displayNames[presetId] ?: presetId,
                    appearance = draft.copy(
                        backgroundType = BackgroundType.PRESET,
                        backgroundPreset = presetId,
                    ),
                    selected = selected,
                    onClick = {
                        onDraftChange(
                            draft.copy(
                                backgroundType = BackgroundType.PRESET,
                                backgroundPreset = presetId,
                                customBackgroundUri = null,
                            ),
                        )
                    },
                )
            }
        }

        Text("Color Themes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorPresets.themes.forEach { theme ->
                val selected = draft.primaryColor == theme.primary &&
                    draft.secondaryColor == theme.secondary &&
                    draft.accentColor == theme.accent
                ColorThemeCard(
                    theme = theme,
                    selected = selected,
                    onClick = {
                        onDraftChange(
                            draft.copy(
                                primaryColor = theme.primary,
                                secondaryColor = theme.secondary,
                                accentColor = theme.accent,
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun PresetBackgroundCard(
    presetId: String,
    label: String,
    appearance: AppearanceSettings,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(3.dp, Color(0xFF3949AB), RoundedCornerShape(16.dp))
                else Modifier,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                ChildBackground(settings = appearance, modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text("Recolorable scene", fontSize = 12.sp, color = Color.Gray)
            }
            if (selected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF3949AB))
            }
        }
    }
}

@Composable
fun ColorThemeCard(
    theme: ColorPresets.Theme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(
                if (selected) Modifier.border(3.dp, Color(0xFF3949AB), RoundedCornerShape(16.dp))
                else Modifier,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ColorDot(theme.primary.toComposeColor())
            ColorDot(theme.secondary.toComposeColor())
            ColorDot(theme.accent.toComposeColor())
            Text(theme.name, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape),
    )
}

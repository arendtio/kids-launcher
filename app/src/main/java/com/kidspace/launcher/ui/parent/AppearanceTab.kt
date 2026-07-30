package com.kidspace.launcher.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kidspace.launcher.ui.theme.presetGradient
import com.kidspace.launcher.ui.theme.toComposeColor

@Composable
fun AppearanceTab(
    settings: AppearanceSettings,
    onSave: (AppearanceSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(settings) { mutableStateOf(settings) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Background Presets", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BackgroundPresets.all.forEach { presetId ->
                    val selected = draft.backgroundPreset == presetId &&
                        draft.backgroundType == BackgroundType.PRESET
                    PresetBackgroundCard(
                        presetId = presetId,
                        label = BackgroundPresets.displayNames[presetId] ?: presetId,
                        selected = selected,
                        onClick = {
                            draft = draft.copy(
                                backgroundType = BackgroundType.PRESET,
                                backgroundPreset = presetId,
                            )
                        },
                    )
                }
            }
        }

        item {
            Text("Color Themes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorPresets.themes.forEach { theme ->
                    val selected = draft.primaryColor == theme.primary &&
                        draft.secondaryColor == theme.secondary &&
                        draft.accentColor == theme.accent
                    ColorThemeCard(
                        theme = theme,
                        selected = selected,
                        onClick = {
                            draft = draft.copy(
                                primaryColor = theme.primary,
                                secondaryColor = theme.secondary,
                                accentColor = theme.accent,
                            )
                        },
                    )
                }
            }
        }

        item {
            Button(
                onClick = { onSave(draft) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Appearance")
            }
        }
    }
}

@Composable
private fun PresetBackgroundCard(
    presetId: String,
    label: String,
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
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(presetGradient(presetId)),
            )
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ColorThemeCard(
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
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color),
    )
}

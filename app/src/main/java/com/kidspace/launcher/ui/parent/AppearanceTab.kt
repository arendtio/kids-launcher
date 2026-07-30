package com.kidspace.launcher.ui.parent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundPresets
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.data.model.ColorPresets
import com.kidspace.launcher.ui.components.ChildBackground
import com.kidspace.launcher.ui.theme.toComposeColor
import java.io.File

@Composable
fun AppearanceTab(
    settings: AppearanceSettings,
    onSave: (AppearanceSettings) -> Unit,
    onImportCustomBackground: (Uri) -> Unit,
    onClearCustomBackground: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(settings) { mutableStateOf(settings) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            onImportCustomBackground(uri)
            draft = draft.copy(backgroundType = BackgroundType.CUSTOM)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Your Photo", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Upload a JPEG or PNG from your device as the child home background.",
                fontSize = 14.sp,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(12.dp))

            val isCustom = draft.backgroundType == BackgroundType.CUSTOM &&
                !draft.customBackgroundUri.isNullOrBlank()

            if (isCustom) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .border(3.dp, Color(0xFF3949AB), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(draft.customBackgroundUri!!))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Custom background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null)
                    Text("  Choose photo", fontSize = 14.sp)
                }
                if (isCustom) {
                    OutlinedButton(
                        onClick = {
                            onClearCustomBackground()
                            draft = draft.copy(
                                backgroundType = BackgroundType.PRESET,
                                customBackgroundUri = null,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Remove", fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Text("Illustrated Backgrounds", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Sharp vector scenes — colors follow your theme below.",
                fontSize = 14.sp,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                            draft = draft.copy(
                                backgroundType = BackgroundType.PRESET,
                                backgroundPreset = presetId,
                                customBackgroundUri = null,
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
                ChildBackground(
                    settings = appearance,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Text(
                    "Recolorable vector scene",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
            if (selected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFF3949AB),
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
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
            .background(color)
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape),
    )
}

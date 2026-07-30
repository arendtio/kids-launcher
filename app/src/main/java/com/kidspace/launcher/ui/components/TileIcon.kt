package com.kidspace.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.util.IconKeyGenerator

@Composable
fun TileIcon(
    type: TileType,
    target: String,
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    tint: Color = Color.White,
) {
    val iconModifier = if (size != null) {
        modifier.size(size)
    } else {
        modifier
    }
    val shape = RoundedCornerShape(12.dp)

    when {
        iconKey.startsWith("app:") -> {
            val context = LocalContext.current
            val packageName = iconKey.removePrefix("app:")
            val drawable = runCatching {
                context.packageManager.getApplicationIcon(packageName)
            }.getOrNull()
            if (drawable != null) {
                AsyncImage(
                    model = drawable,
                    contentDescription = null,
                    modifier = iconModifier.clip(shape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                FallbackIcon(iconKey, iconModifier, size, tint)
            }
        }
        iconKey.startsWith("favicon:") || iconKey.startsWith("http") -> {
            val url = if (iconKey.startsWith("http")) iconKey else IconKeyGenerator.faviconUrl(target)
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = iconModifier.clip(shape),
                contentScale = ContentScale.Crop,
                loading = {
                    FallbackIcon(iconKey, Modifier.fillMaxSize(), size, tint.copy(alpha = 0.5f))
                },
                error = {
                    FallbackIcon(iconKey, Modifier.fillMaxSize(), size, tint)
                },
            )
        }
        else -> FallbackIcon(iconKey, iconModifier, size, tint)
    }
}

@Composable
private fun FallbackIcon(
    iconKey: String,
    modifier: Modifier,
    size: Dp?,
    tint: Color,
) {
    val iconName = iconKey.removePrefix("random:")
    val vector = randomIconVector(iconName)
    val boxModifier = if (size != null) modifier.size(size) else modifier
    Box(
        modifier = boxModifier
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = tint,
            modifier = if (size != null) Modifier.size(size * 0.55f) else Modifier.fillMaxSize(0.65f),
        )
    }
}

private fun randomIconVector(name: String): ImageVector = when (name) {
    "heart" -> Icons.Default.Favorite
    "rocket" -> Icons.Default.Flight
    "flower" -> Icons.Default.LocalFlorist
    "moon" -> Icons.Default.NightsStay
    "sun" -> Icons.Default.WbSunny
    "fish", "butterfly", "balloon", "rainbow" -> Icons.Default.Pets
    else -> Icons.Default.Star
}

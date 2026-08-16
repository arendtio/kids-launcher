package com.kidspace.launcher.ui.components

import android.content.pm.LauncherApps
import android.os.Process
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
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

/**
 * Corner radius for raster tile icons (PWA, thumbnails).
 *
 * Android adaptive icons use an OEM mask on a 108dp canvas with a 66dp safe zone; at typical
 * launcher sizes that reads as roughly 12% corner radius — not the 30% used for Play Store
 * marketing assets. See developer.android.com/develop/ui/compose/system/icon_design_adaptive
 */
private const val RasterIconCornerPercent = 12

/** Inset mimics adaptive-icon safe zone so full-bleed artwork is not clipped at the corners. */
private const val RasterIconInsetFraction = 0.92f

private val RasterIconShape: Shape = RoundedCornerShape(percent = RasterIconCornerPercent)

private fun rasterIconShape(size: Dp?): Shape {
    if (size == null) return RasterIconShape
    val radius = (size.value * RasterIconCornerPercent / 100f).coerceIn(8f, 16f).dp
    return RoundedCornerShape(radius)
}

@Composable
fun TileIcon(
    type: TileType,
    target: String,
    iconKey: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    tint: Color = Color.White,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val iconModifier = if (size != null) {
        modifier.size(size)
    } else {
        modifier
    }
    val resolvedScale = resolveContentScale(type, iconKey, contentScale)
    val clipShape = if (usesRasterClip(iconKey, type)) RasterIconShape else RectangleShape

    when {
        iconKey.startsWith("app:") -> {
            val context = LocalContext.current
            val packageName = iconKey.removePrefix("app:")
            val drawable = runCatching {
                context.packageManager.getApplicationIcon(packageName)
            }.getOrNull()
            if (drawable != null) {
                ClippedIconImage(
                    modifier = iconModifier,
                    shape = clipShape,
                ) {
                    AsyncImage(
                        model = drawable,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = resolvedScale,
                    )
                }
            } else {
                FallbackIcon(iconKey, iconModifier, size, tint)
            }
        }
        iconKey.startsWith("youtube:") -> {
            val url = IconKeyGenerator.youtubeThumbnailUrl(iconKey)
            if (url == null) {
                FallbackIcon(iconKey, iconModifier, size, tint)
            } else {
                ClippedIconImage(
                    modifier = iconModifier,
                    shape = rasterIconShape(size),
                    insetFraction = RasterIconInsetFraction,
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = resolvedScale,
                        loading = {
                            FallbackIcon(iconKey, Modifier.fillMaxSize(), size, tint.copy(alpha = 0.5f))
                        },
                        error = {
                            FallbackIcon(iconKey, Modifier.fillMaxSize(), size, tint)
                        },
                    )
                }
            }
        }
        iconKey.startsWith("shortcut:") -> {
            val context = LocalContext.current
            val parsed = IconKeyGenerator.parseShortcutIconKey(iconKey)
            val drawable = if (parsed != null) {
                val (hostPackage, shortcutId) = parsed
                val launcherApps = context.getSystemService(LauncherApps::class.java)
                runCatching {
                    val query = LauncherApps.ShortcutQuery().apply {
                        setPackage(hostPackage)
                        setShortcutIds(listOf(shortcutId))
                        setQueryFlags(
                            LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC,
                        )
                    }
                    val shortcut = launcherApps?.getShortcuts(query, Process.myUserHandle())?.firstOrNull()
                    shortcut?.let {
                        launcherApps.getShortcutIconDrawable(
                            it,
                            context.resources.displayMetrics.densityDpi,
                        )
                    }
                }.getOrNull()
            } else {
                null
            }
            if (drawable != null) {
                ClippedIconImage(
                    modifier = iconModifier,
                    shape = clipShape,
                ) {
                    AsyncImage(
                        model = drawable,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = resolvedScale,
                    )
                }
            } else {
                FallbackIcon("random:star", iconModifier, size, tint)
            }
        }
        iconKey.startsWith("legacy:") -> {
            FallbackIcon("random:star", iconModifier, size, tint)
        }
        iconKey.startsWith("favicon:") || iconKey.startsWith("http") -> {
            val url = if (iconKey.startsWith("http")) {
                iconKey
            } else {
                IconKeyGenerator.faviconUrl(target)
            }
            ClippedIconImage(
                modifier = iconModifier,
                shape = rasterIconShape(size),
                insetFraction = RasterIconInsetFraction,
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = resolvedScale,
                    loading = {
                        FallbackIcon(iconKey, Modifier.fillMaxSize(), size, tint.copy(alpha = 0.5f))
                    },
                    error = {
                        FallbackIcon(iconKey, Modifier.fillMaxSize(), size, tint)
                    },
                )
            }
        }
        else -> FallbackIcon(iconKey, iconModifier, size, tint)
    }
}

@Composable
private fun ClippedIconImage(
    modifier: Modifier,
    shape: Shape,
    insetFraction: Float = 1f,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = if (insetFraction >= 1f) {
                Modifier.fillMaxSize()
            } else {
                Modifier.fillMaxSize(insetFraction)
            },
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

private fun usesRasterClip(iconKey: String, type: TileType): Boolean {
    return type == TileType.WEBSITE ||
        type == TileType.YOUTUBE ||
        iconKey.startsWith("favicon:") ||
        iconKey.startsWith("http") ||
        iconKey.startsWith("youtube:")
}

private fun resolveContentScale(
    type: TileType,
    iconKey: String,
    requested: ContentScale,
): ContentScale {
    if (requested != ContentScale.Fit) return requested
    return when {
        type == TileType.WEBSITE ||
            type == TileType.YOUTUBE ||
            iconKey.startsWith("favicon:") ||
            iconKey.startsWith("http") ||
            iconKey.startsWith("youtube:") -> ContentScale.Crop
        else -> ContentScale.Fit
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

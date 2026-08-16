package com.kidspace.launcher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundPresets
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.ui.theme.toComposeColor
import java.io.File

@Composable
fun ChildBackground(
    settings: AppearanceSettings,
    modifier: Modifier = Modifier,
) {
    val primary = settings.primaryColor.toComposeColor()
    val secondary = settings.secondaryColor.toComposeColor()
    val accent = settings.accentColor.toComposeColor()

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            },
    ) {
        when (settings.backgroundType) {
            BackgroundType.CUSTOM -> {
                val uri = settings.customBackgroundUri
                val context = LocalContext.current
                if (!uri.isNullOrBlank()) {
                    AsyncImage(
                        model = remember(uri, context) {
                            ImageRequest.Builder(context)
                                .data(if (uri.startsWith("/")) File(uri) else uri)
                                .crossfade(false)
                                .build()
                        },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    PresetBackgroundScene(
                        presetId = settings.backgroundPreset,
                        primary = primary,
                        secondary = secondary,
                        accent = accent,
                        animationsEnabled = settings.backgroundAnimationsEnabled,
                    )
                }
            }
            BackgroundType.PRESET -> {
                PresetBackgroundScene(
                    presetId = settings.backgroundPreset,
                    primary = primary,
                    secondary = secondary,
                    accent = accent,
                    animationsEnabled = settings.backgroundAnimationsEnabled,
                )
            }
        }
    }
}

@Composable
fun PresetBackgroundScene(
    presetId: String,
    primary: Color,
    secondary: Color,
    accent: Color,
    animationsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val resolvedId = if (presetId == "sunny_meadow") BackgroundPresets.SKY_MEADOW else presetId
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    if (animationsEnabled) {
                        when (resolvedId) {
                            BackgroundPresets.OCEAN_BUBBLES -> drawOceanScene(primary, secondary, accent)
                            BackgroundPresets.CANDY_CLOUDS -> drawCandyCloudScene(primary, secondary, accent)
                            BackgroundPresets.STARRY_NIGHT -> drawStarryNightScene(primary, secondary, accent)
                            BackgroundPresets.RAINBOW_HILLS -> drawRainbowHillsScene(primary, secondary, accent)
                            else -> drawSkyMeadowScene(primary, secondary, accent)
                        }
                    } else {
                        drawSimpleGradientScene(resolvedId, primary, secondary, accent)
                    }
                }
            },
    )
}

private fun DrawScope.drawSimpleGradientScene(
    presetId: String,
    primary: Color,
    secondary: Color,
    accent: Color,
) {
    val colors = when (presetId) {
        BackgroundPresets.OCEAN_BUBBLES -> listOf(
            secondary.copy(alpha = 0.9f),
            primary.copy(alpha = 0.95f),
            primary.darken(0.15f),
        )
        BackgroundPresets.CANDY_CLOUDS -> listOf(
            blend(primary, Color.White, 0.35f),
            blend(secondary, Color.White, 0.25f),
            blend(accent, Color.White, 0.45f),
        )
        BackgroundPresets.STARRY_NIGHT -> listOf(
            primary.darken(0.55f),
            blend(primary, Color(0xFF1A237E), 0.5f),
            primary.darken(0.65f),
        )
        BackgroundPresets.RAINBOW_HILLS -> listOf(
            secondary.lighten(0.35f),
            primary.lighten(0.25f),
            accent.lighten(0.15f),
        )
        else -> listOf(
            primary.copy(alpha = 0.85f),
            secondary.copy(alpha = 0.75f),
            secondary.copy(alpha = 0.55f),
        )
    }
    drawRect(brush = Brush.verticalGradient(colors))
}

private fun DrawScope.drawSkyMeadowScene(primary: Color, secondary: Color, accent: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                primary.copy(alpha = 0.85f),
                secondary.copy(alpha = 0.75f),
                secondary.copy(alpha = 0.55f),
            ),
        ),
    )

    drawCircle(
        color = accent,
        radius = size.width * 0.09f,
        center = Offset(size.width * 0.82f, size.height * 0.12f),
    )
    drawCircle(
        color = accent.copy(alpha = 0.35f),
        radius = size.width * 0.11f,
        center = Offset(size.width * 0.82f, size.height * 0.12f),
    )

    drawCloud(this, Offset(size.width * 0.2f, size.height * 0.14f), size.width * 0.16f, Color.White.copy(0.95f))
    drawCloud(this, Offset(size.width * 0.55f, size.height * 0.08f), size.width * 0.12f, Color.White.copy(0.9f))

    val hillDark = blend(secondary, Color(0xFF2E7D32), 0.45f)
    val hillLight = blend(secondary, Color(0xFF81C784), 0.55f)
    drawHill(this, size.height * 0.72f, hillDark, 0f)
    drawHill(this, size.height * 0.78f, hillLight, size.width * 0.25f)

    drawFlower(this, Offset(size.width * 0.18f, size.height * 0.68f), accent)
    drawFlower(this, Offset(size.width * 0.72f, size.height * 0.74f), primary)
    drawFlower(this, Offset(size.width * 0.45f, size.height * 0.7f), accent.copy(alpha = 0.85f))
}

private fun DrawScope.drawOceanScene(primary: Color, secondary: Color, accent: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                secondary.copy(alpha = 0.9f),
                primary.copy(alpha = 0.95f),
                primary.darken(0.15f),
            ),
            startY = 0f,
            endY = size.height,
        ),
    )

    drawCloud(this, Offset(size.width * 0.15f, size.height * 0.1f), size.width * 0.1f, Color.White.copy(0.85f))
    drawCloud(this, Offset(size.width * 0.7f, size.height * 0.06f), size.width * 0.13f, Color.White.copy(0.8f))

    repeat(4) { i ->
        val waveY = size.height * (0.42f + i * 0.12f)
        drawWave(this, waveY, primary.lighten(0.1f + i * 0.04f), i * 40f)
    }

    listOf(0.2f, 0.45f, 0.68f, 0.85f).forEachIndexed { i, x ->
        drawCircle(
            color = Color.White.copy(alpha = 0.25f + i * 0.05f),
            radius = size.width * (0.02f + i * 0.008f),
            center = Offset(size.width * x, size.height * (0.55f + i * 0.08f)),
        )
    }

    drawFish(this, Offset(size.width * 0.3f, size.height * 0.58f), size.width * 0.08f, accent)
    drawFish(this, Offset(size.width * 0.75f, size.height * 0.65f), size.width * 0.06f, accent.lighten(0.2f))
}

private fun DrawScope.drawCandyCloudScene(primary: Color, secondary: Color, accent: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                blend(primary, Color.White, 0.35f),
                blend(secondary, Color.White, 0.25f),
                blend(accent, Color.White, 0.45f),
            ),
        ),
    )

    drawCloud(this, Offset(size.width * 0.25f, size.height * 0.2f), size.width * 0.2f, Color.White)
    drawCloud(this, Offset(size.width * 0.7f, size.height * 0.28f), size.width * 0.17f, Color.White.copy(0.95f))
    drawCloud(this, Offset(size.width * 0.5f, size.height * 0.12f), size.width * 0.14f, Color.White.copy(0.9f))

    val sparkles = listOf(
        Offset(size.width * 0.15f, size.height * 0.45f),
        Offset(size.width * 0.85f, size.height * 0.4f),
        Offset(size.width * 0.6f, size.height * 0.55f),
        Offset(size.width * 0.35f, size.height * 0.62f),
    )
    sparkles.forEach { center ->
        drawStar(this, center, size.width * 0.025f, accent)
        drawStar(this, center + Offset(size.width * 0.04f, -size.height * 0.02f), size.width * 0.015f, primary)
    }

    drawHeart(this, Offset(size.width * 0.2f, size.height * 0.75f), size.width * 0.05f, accent.copy(0.8f))
    drawHeart(this, Offset(size.width * 0.78f, size.height * 0.7f), size.width * 0.04f, primary.copy(0.75f))
}

private fun DrawScope.drawStarryNightScene(primary: Color, secondary: Color, accent: Color) {
    val night = primary.darken(0.55f)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(night, blend(primary, Color(0xFF1A237E), 0.5f), night.darken(0.1f)),
        ),
    )

    drawCircle(
        color = secondary.copy(alpha = 0.95f),
        radius = size.width * 0.07f,
        center = Offset(size.width * 0.78f, size.height * 0.14f),
    )
    drawCircle(
        color = night,
        radius = size.width * 0.055f,
        center = Offset(size.width * 0.8f, size.height * 0.13f),
    )

    repeat(28) { i ->
        val x = (i * 73 % 100) / 100f * size.width
        val y = (i * 41 % 100) / 100f * size.height * 0.75f
        val starColor = if (i % 4 == 0) accent else Color.White.copy(alpha = 0.7f + (i % 3) * 0.1f)
        drawCircle(color = starColor, radius = size.width * 0.004f * (1 + i % 3), center = Offset(x, y))
    }

    drawHill(this, size.height * 0.88f, night.lighten(0.12f), 0f)
    drawHill(this, size.height * 0.92f, blend(secondary, night, 0.6f), size.width * 0.3f)
}

private fun DrawScope.drawRainbowHillsScene(primary: Color, secondary: Color, accent: Color) {
    drawRect(brush = Brush.verticalGradient(listOf(secondary.lighten(0.35f), primary.lighten(0.25f))))

    val rainbowColors = listOf(accent, primary, secondary, accent.lighten(0.15f), primary.lighten(0.2f))
    val arcLeft = size.width * 0.05f
    val arcTop = size.height * 0.18f
    val arcSize = Size(size.width * 0.9f, size.height * 0.45f)
    rainbowColors.forEachIndexed { i, color ->
        drawArc(
            color = color.copy(alpha = 0.9f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(arcLeft + i * 8f, arcTop + i * 8f),
            size = Size(arcSize.width - i * 16f, arcSize.height - i * 16f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = size.width * 0.018f),
        )
    }

    drawCloud(this, Offset(size.width * 0.12f, size.height * 0.1f), size.width * 0.09f, Color.White.copy(0.9f))
    drawHill(this, size.height * 0.8f, blend(primary, Color(0xFF43A047), 0.35f), 0f)
    drawHill(this, size.height * 0.86f, blend(secondary, Color(0xFF66BB6A), 0.4f), size.width * 0.2f)
}

private fun drawCloud(scope: DrawScope, center: Offset, scale: Float, color: Color) {
    with(scope) {
        drawCircle(color, scale * 0.35f, center + Offset(-scale * 0.35f, 0f))
        drawCircle(color, scale * 0.45f, center)
        drawCircle(color, scale * 0.38f, center + Offset(scale * 0.38f, scale * 0.05f))
        drawRoundRect(
            color = color,
            topLeft = center + Offset(-scale * 0.55f, scale * 0.05f),
            size = Size(scale * 1.1f, scale * 0.35f),
        )
    }
}

private fun drawHill(scope: DrawScope, baseY: Float, color: Color, xOffset: Float) {
    with(scope) {
        val path = Path().apply {
            moveTo(-size.width * 0.1f + xOffset, size.height)
            cubicTo(
                size.width * 0.15f + xOffset, baseY - size.height * 0.08f,
                size.width * 0.45f + xOffset, baseY + size.height * 0.04f,
                size.width * 1.1f, size.height,
            )
            close()
        }
        drawPath(path, color, style = Fill)
    }
}

private fun drawWave(scope: DrawScope, y: Float, color: Color, phase: Float) {
    with(scope) {
        val path = Path().apply {
            moveTo(0f, y)
            var x = 0f
            while (x <= size.width) {
                val wave = kotlin.math.sin((x + phase) / size.width * 6.28f) * size.height * 0.02f
                lineTo(x, y + wave)
                x += size.width / 20f
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color.copy(alpha = 0.55f), style = Fill)
    }
}

private fun drawFish(scope: DrawScope, center: Offset, bodyW: Float, color: Color) {
    with(scope) {
        drawOval(color, topLeft = center - Offset(bodyW, bodyW * 0.4f), size = Size(bodyW * 2f, bodyW * 0.8f))
        val tail = Path().apply {
            moveTo(center.x - bodyW, center.y)
            lineTo(center.x - bodyW * 1.5f, center.y - bodyW * 0.45f)
            lineTo(center.x - bodyW * 1.5f, center.y + bodyW * 0.45f)
            close()
        }
        drawPath(tail, color, style = Fill)
        drawCircle(Color.White, bodyW * 0.12f, center + Offset(bodyW * 0.45f, -bodyW * 0.12f))
        drawCircle(Color.Black.copy(0.6f), bodyW * 0.05f, center + Offset(bodyW * 0.48f, -bodyW * 0.12f))
    }
}

private fun drawFlower(scope: DrawScope, center: Offset, color: Color) {
    with(scope) {
        repeat(5) { i ->
            val angle = i * 72f * (Math.PI / 180f)
            val petalCenter = center + Offset(
                (kotlin.math.cos(angle) * size.width * 0.012f).toFloat(),
                (kotlin.math.sin(angle) * size.width * 0.012f).toFloat(),
            )
            drawCircle(color.copy(alpha = 0.9f), size.width * 0.008f, petalCenter)
        }
        drawCircle(Color.White.copy(0.9f), size.width * 0.006f, center)
    }
}

private fun drawStar(scope: DrawScope, center: Offset, radius: Float, color: Color) {
    with(scope) {
        val path = Path()
        repeat(5) { i ->
            val outerAngle = Math.PI / 2 + i * 2 * Math.PI / 5
            val innerAngle = outerAngle + Math.PI / 5
            val outer = center + Offset(
                (kotlin.math.cos(outerAngle) * radius).toFloat(),
                (-kotlin.math.sin(outerAngle) * radius).toFloat(),
            )
            val inner = center + Offset(
                (kotlin.math.cos(innerAngle) * radius * 0.45f).toFloat(),
                (-kotlin.math.sin(innerAngle) * radius * 0.45f).toFloat(),
            )
            if (i == 0) path.moveTo(outer.x, outer.y) else path.lineTo(outer.x, outer.y)
            path.lineTo(inner.x, inner.y)
        }
        path.close()
        drawPath(path, color, style = Fill)
    }
}

private fun drawHeart(scope: DrawScope, center: Offset, size: Float, color: Color) {
    with(scope) {
        drawCircle(color, size * 0.5f, center + Offset(-size * 0.25f, -size * 0.15f))
        drawCircle(color, size * 0.5f, center + Offset(size * 0.25f, -size * 0.15f))
        val path = Path().apply {
            moveTo(center.x - size * 0.55f, center.y - size * 0.05f)
            cubicTo(
                center.x, center.y + size * 0.9f,
                center.x, center.y + size * 0.9f,
                center.x + size * 0.55f, center.y - size * 0.05f,
            )
            close()
        }
        drawPath(path, color, style = Fill)
    }
}

private fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

private fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha,
    )
}

private fun blend(a: Color, b: Color, ratio: Float): Color {
    return Color(
        red = a.red * (1 - ratio) + b.red * ratio,
        green = a.green * (1 - ratio) + b.green * ratio,
        blue = a.blue * (1 - ratio) + b.blue * ratio,
        alpha = a.alpha,
    )
}

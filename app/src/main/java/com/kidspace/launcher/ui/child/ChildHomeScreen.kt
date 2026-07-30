package com.kidspace.launcher.ui.child

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.ui.components.ChildBackground
import com.kidspace.launcher.ui.components.TileIcon
import com.kidspace.launcher.ui.theme.toComposeColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChildHomeScreen(
    tiles: List<ChildTile>,
    appearance: AppearanceSettings,
    onTileClick: (ChildTile) -> Unit,
    onAppearanceTileClick: () -> Unit,
    onParentAccessRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = appearance.primaryColor.toComposeColor()
    val secondary = appearance.secondaryColor.toComposeColor()
    val accent = appearance.accentColor.toComposeColor()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columnCount = if (isLandscape) 6 else 4
    val contentPadding = if (isLandscape) 12.dp else 20.dp

    Box(modifier = modifier.fillMaxSize()) {
        ChildBackground(
            settings = appearance,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            ChildHeader(
                primary = primary,
                secondary = secondary,
                compact = isLandscape,
                onParentAccessRequest = onParentAccessRequest,
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (tiles.isEmpty()) {
                    item(span = { GridItemSpan(columnCount) }) {
                        EmptyChildState(accent = accent, compact = isLandscape)
                    }
                } else {
                    items(tiles, key = { it.id }) { tile ->
                        ChildTileCard(
                            tile = tile,
                            primary = primary,
                            onClick = { onTileClick(tile) },
                        )
                    }
                }
                item(key = "appearance_tile") {
                    AppearanceTileCard(
                        primary = primary,
                        accent = accent,
                        onClick = onAppearanceTileClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChildHeader(
    primary: Color,
    secondary: Color,
    compact: Boolean,
    onParentAccessRequest: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onParentAccessRequest,
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = primary.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = if (compact) 12.dp else 20.dp,
                    horizontal = if (compact) 16.dp else 24.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "✨ KidSpace ✨",
                fontSize = if (compact) 24.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap your favorites below!",
                fontSize = if (compact) 14.sp else 16.sp,
                color = Color.White.copy(alpha = 0.95f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            if (!compact) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Parents: long-press here for parent mode",
                    fontSize = 13.sp,
                    color = secondary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyChildState(accent: Color, compact: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "🌈", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No apps yet!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
                Text(
                    text = "Ask a grown-up to add some favorites.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppearanceTileCard(
    primary: Color,
    accent: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxWidth(0.55f).aspectRatio(1f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "My Look",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 13.sp,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChildTileCard(
    tile: ChildTile,
    primary: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                TileIcon(
                    type = tile.type,
                    target = tile.target,
                    iconKey = tile.iconKey,
                    modifier = Modifier.fillMaxSize(),
                    tint = primary,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tile.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

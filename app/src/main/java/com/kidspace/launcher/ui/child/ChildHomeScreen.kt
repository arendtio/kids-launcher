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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onParentAccessRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = appearance.primaryColor.toComposeColor()
    val secondary = appearance.secondaryColor.toComposeColor()
    val accent = appearance.accentColor.toComposeColor()

    Box(modifier = modifier.fillMaxSize()) {
        ChildBackground(
            settings = appearance,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            ChildHeader(
                primary = primary,
                secondary = secondary,
                onParentAccessRequest = onParentAccessRequest,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (tiles.isEmpty()) {
                EmptyChildState(accent = accent)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(tiles, key = { it.id }) { tile ->
                        ChildTileCard(
                            tile = tile,
                            primary = primary,
                            onClick = { onTileClick(tile) },
                        )
                    }
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
                .padding(vertical = 20.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "✨ KidSpace ✨",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap your favorites below!",
                fontSize = 16.sp,
                color = secondary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EmptyChildState(accent: Color) {
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

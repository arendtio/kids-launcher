package com.kidspace.launcher.ui.parent

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.InstalledApp
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.ui.components.TileIcon
import com.kidspace.launcher.util.UrlValidator

enum class ParentTab { TILES, APPS, APPEARANCE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentModeScreen(
    tiles: List<ChildTile>,
    installedApps: List<InstalledApp>,
    appearance: AppearanceSettings,
    onExitParentMode: () -> Unit,
    onAddApp: (InstalledApp) -> Unit,
    onAddLink: (label: String, url: String, type: TileType) -> Unit,
    onRemoveTile: (Long) -> Unit,
    onMoveTile: (Long, Int) -> Unit,
    onSaveAppearance: (AppearanceSettings) -> Unit,
    onImportCustomBackground: (android.net.Uri) -> Unit,
    onClearCustomBackground: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(ParentTab.TILES.ordinal) }
    var showAddLinkDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Parent Mode", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onExitParentMode) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3949AB),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == ParentTab.TILES.ordinal,
                    onClick = { selectedTab = ParentTab.TILES.ordinal },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Tiles") },
                )
                NavigationBarItem(
                    selected = selectedTab == ParentTab.APPS.ordinal,
                    onClick = { selectedTab = ParentTab.APPS.ordinal },
                    icon = { Icon(Icons.Default.Apps, null) },
                    label = { Text("Apps") },
                )
                NavigationBarItem(
                    selected = selectedTab == ParentTab.APPEARANCE.ordinal,
                    onClick = { selectedTab = ParentTab.APPEARANCE.ordinal },
                    icon = { Icon(Icons.Default.Palette, null) },
                    label = { Text("Look") },
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == ParentTab.TILES.ordinal) {
                FloatingActionButton(
                    onClick = { showAddLinkDialog = true },
                    containerColor = Color(0xFFFFD54F),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add link")
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (ParentTab.entries[selectedTab]) {
                ParentTab.TILES -> TilesEditorTab(
                    tiles = tiles,
                    onRemove = onRemoveTile,
                    onMove = onMoveTile,
                )
                ParentTab.APPS -> AppsTab(
                    apps = installedApps,
                    existingTargets = tiles.map { it.target }.toSet(),
                    onAdd = onAddApp,
                )
                ParentTab.APPEARANCE -> AppearanceTab(
                    settings = appearance,
                    onSave = onSaveAppearance,
                    onImportCustomBackground = onImportCustomBackground,
                    onClearCustomBackground = onClearCustomBackground,
                )
            }
        }
    }

    if (showAddLinkDialog) {
        AddLinkDialog(
            onDismiss = { showAddLinkDialog = false },
            onConfirm = { label, url, type ->
                onAddLink(label, url, type)
                showAddLinkDialog = false
            },
        )
    }
}

@Composable
private fun TilesEditorTab(
    tiles: List<ChildTile>,
    onRemove: (Long) -> Unit,
    onMove: (Long, Int) -> Unit,
) {
    if (tiles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tiles yet. Add apps or links!", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tiles, key = { it.id }) { tile ->
            val index = tiles.indexOf(tile)
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TileIcon(
                        type = tile.type,
                        target = tile.target,
                        iconKey = tile.iconKey,
                        size = 48.dp,
                    )
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(tile.label, fontWeight = FontWeight.Bold)
                        Text(
                            tile.target,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                        )
                    }
                    IconButton(
                        onClick = { if (index > 0) onMove(tile.id, index - 1) },
                        enabled = index > 0,
                    ) {
                        Icon(Icons.Default.ArrowUpward, "Move up")
                    }
                    IconButton(
                        onClick = { if (index < tiles.lastIndex) onMove(tile.id, index + 1) },
                        enabled = index < tiles.lastIndex,
                    ) {
                        Icon(Icons.Default.ArrowDownward, "Move down")
                    }
                    IconButton(onClick = { onRemove(tile.id) }) {
                        Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppsTab(
    apps: List<InstalledApp>,
    existingTargets: Set<String>,
    onAdd: (InstalledApp) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(apps, key = { it.packageName }) { app ->
            val alreadyAdded = app.packageName in existingTargets
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !alreadyAdded) { onAdd(app) },
                colors = CardDefaults.cardColors(
                    containerColor = if (alreadyAdded) Color.LightGray.copy(0.3f) else Color.White,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TileIcon(
                        type = TileType.APP,
                        target = app.packageName,
                        iconKey = "app:${app.packageName}",
                        size = 40.dp,
                    )
                    Text(
                        text = app.label,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Medium,
                    )
                    if (alreadyAdded) {
                        Text("Added", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        Icon(Icons.Default.Add, "Add", tint = Color(0xFF3949AB))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, url: String, type: TileType) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var linkType by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Link") },
        text = {
            Column {
                TabRow(selectedTabIndex = linkType) {
                    Tab(selected = linkType == 0, onClick = { linkType = 0 }, text = { Text("Website") })
                    Tab(selected = linkType == 1, onClick = { linkType = 1 }, text = { Text("YouTube") })
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(if (linkType == 1) "YouTube URL" else "Website URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (error != null) {
                    Text(error!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (label.isBlank()) {
                    error = "Please enter a name"
                    return@TextButton
                }
                if (!UrlValidator.isValidUrl(url)) {
                    error = "Please enter a valid URL"
                    return@TextButton
                }
                val type = if (linkType == 1 || UrlValidator.isYouTubeUrl(url)) {
                    TileType.YOUTUBE
                } else {
                    TileType.WEBSITE
                }
                onConfirm(label.trim(), url.trim(), type)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

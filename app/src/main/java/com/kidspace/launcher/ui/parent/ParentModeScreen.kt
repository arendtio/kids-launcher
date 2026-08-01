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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.runtime.LaunchedEffect
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
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
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.data.model.WebLaunchMode
import com.kidspace.launcher.data.model.WebLinkConfig
import com.kidspace.launcher.ui.components.TileIcon
import com.kidspace.launcher.util.UrlValidator
import com.kidspace.launcher.youtube.YouTubeSearchResult

enum class ParentTab { TILES, APPS, VIDEOS, APPEARANCE, BACKUP }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentModeScreen(
    tiles: List<ChildTile>,
    installedApps: List<InstalledApp>,
    appearance: AppearanceSettings,
    onExitParentMode: () -> Unit,
    onAddApp: (InstalledApp) -> Unit,
    onRemoveApp: (InstalledApp) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onAddLink: (label: String, url: String, type: TileType, webConfig: WebLinkConfig) -> Unit,
    onUpdateTile: (ChildTile) -> Unit,
    onRemoveTile: (Long) -> Unit,
    onReorderTiles: (fromIndex: Int, toIndex: Int) -> Unit,
    onSaveAppearance: (AppearanceSettings) -> Unit,
    onImportCustomBackground: (android.net.Uri) -> Unit,
    onClearCustomBackground: () -> Unit,
    onExportBackup: (android.net.Uri) -> Unit,
    onImportBackup: (android.net.Uri) -> Unit,
    backupStatusMessage: String?,
    backupStatusIsError: Boolean,
    onDismissBackupStatus: () -> Unit,
    appVersionName: String,
    isUpdateDownloading: Boolean,
    updateStatusMessage: String?,
    updateStatusIsError: Boolean,
    canInstallUpdate: Boolean,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onDismissUpdateStatus: () -> Unit,
    youtubeQuery: String,
    isYouTubeSearching: Boolean,
    youtubeResults: List<YouTubeSearchResult>,
    selectedYouTubeVideoIds: Set<String>,
    childYouTubeVideoIds: Set<String>,
    youtubeSearchErrorMessage: String?,
    youtubeSearchStatusMessage: String?,
    onYouTubeQueryChange: (String) -> Unit,
    onYouTubeSearch: () -> Unit,
    onToggleYouTubeSelection: (String) -> Unit,
    onSelectAllYouTubeResults: () -> Unit,
    onClearYouTubeSelection: () -> Unit,
    onAddSelectedYouTubeVideos: () -> Unit,
    onDismissYouTubeSearchStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(ParentTab.TILES.ordinal) }
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var editingTile by remember { mutableStateOf<ChildTile?>(null) }

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
                    selected = selectedTab == ParentTab.VIDEOS.ordinal,
                    onClick = { selectedTab = ParentTab.VIDEOS.ordinal },
                    icon = { Icon(Icons.Default.VideoLibrary, null) },
                    label = { Text("Videos") },
                )
                NavigationBarItem(
                    selected = selectedTab == ParentTab.APPEARANCE.ordinal,
                    onClick = { selectedTab = ParentTab.APPEARANCE.ordinal },
                    icon = { Icon(Icons.Default.Palette, null) },
                    label = { Text("Look") },
                )
                NavigationBarItem(
                    selected = selectedTab == ParentTab.BACKUP.ordinal,
                    onClick = { selectedTab = ParentTab.BACKUP.ordinal },
                    icon = { Icon(Icons.Default.Save, null) },
                    label = { Text("Backup") },
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
                    onEdit = { editingTile = it },
                    onRemove = onRemoveTile,
                    onReorder = onReorderTiles,
                )
                ParentTab.APPS -> AppsTab(
                    apps = installedApps,
                    childAppPackages = tiles
                        .filter { it.type == TileType.APP }
                        .map { it.target }
                        .toSet(),
                    onAdd = onAddApp,
                    onRemove = onRemoveApp,
                    onLaunch = onLaunchApp,
                )
                ParentTab.VIDEOS -> YouTubeSearchTab(
                    query = youtubeQuery,
                    isSearching = isYouTubeSearching,
                    results = youtubeResults,
                    selectedVideoIds = selectedYouTubeVideoIds,
                    childYouTubeVideoIds = childYouTubeVideoIds,
                    errorMessage = youtubeSearchErrorMessage,
                    statusMessage = youtubeSearchStatusMessage,
                    onQueryChange = onYouTubeQueryChange,
                    onSearch = onYouTubeSearch,
                    onToggleSelection = onToggleYouTubeSelection,
                    onSelectAll = onSelectAllYouTubeResults,
                    onClearSelection = onClearYouTubeSelection,
                    onAddSelected = onAddSelectedYouTubeVideos,
                    onDismissStatus = onDismissYouTubeSearchStatus,
                )
                ParentTab.APPEARANCE -> AppearanceTab(
                    settings = appearance,
                    onSave = onSaveAppearance,
                    onImportCustomBackground = onImportCustomBackground,
                    onClearCustomBackground = onClearCustomBackground,
                )
                ParentTab.BACKUP -> BackupTab(
                    tileCount = tiles.size,
                    statusMessage = backupStatusMessage,
                    statusIsError = backupStatusIsError,
                    onExport = onExportBackup,
                    onImport = onImportBackup,
                    onDismissStatus = onDismissBackupStatus,
                    appVersionName = appVersionName,
                    isUpdateDownloading = isUpdateDownloading,
                    updateStatusMessage = updateStatusMessage,
                    updateStatusIsError = updateStatusIsError,
                    canInstallUpdate = canInstallUpdate,
                    onDownloadUpdate = onDownloadUpdate,
                    onInstallUpdate = onInstallUpdate,
                    onDismissUpdateStatus = onDismissUpdateStatus,
                )
            }
        }
    }

    if (showAddLinkDialog) {
        LinkTileDialog(
            onDismiss = { showAddLinkDialog = false },
            onConfirm = { label, url, type, webConfig ->
                onAddLink(label, url, type, webConfig)
                showAddLinkDialog = false
            },
        )
    }

    editingTile?.let { tile ->
        when (tile.type) {
            TileType.APP -> AppTileEditDialog(
                tile = tile,
                onDismiss = { editingTile = null },
                onConfirm = { updated ->
                    onUpdateTile(updated)
                    editingTile = null
                },
            )
            TileType.WEBSITE, TileType.YOUTUBE -> LinkTileDialog(
                existingTile = tile,
                onDismiss = { editingTile = null },
                onConfirm = { label, url, type, webConfig ->
                    onUpdateTile(
                        tile.copy(
                            label = label,
                            target = url,
                            type = type,
                            webLaunchMode = webConfig.webLaunchMode,
                            cameraPolicy = webConfig.cameraPolicy,
                            microphonePolicy = webConfig.microphonePolicy,
                            locationPolicy = webConfig.locationPolicy,
                        ),
                    )
                    editingTile = null
                },
            )
        }
    }
}

@Composable
private fun TilesEditorTab(
    tiles: List<ChildTile>,
    onEdit: (ChildTile) -> Unit,
    onRemove: (Long) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    if (tiles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tiles yet. Add apps or links!", color = Color.Gray)
        }
        return
    }

    var localTiles by remember { mutableStateOf(tiles) }
    LaunchedEffect(tiles) {
        localTiles = tiles
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localTiles = localTiles.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        onReorder(from.index, to.index)
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(localTiles, key = { it.id }) { tile ->
            var showMenu by remember(tile.id) { mutableStateOf(false) }
            ReorderableItem(reorderableState, key = tile.id) { isDragging ->
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 8.dp else 4.dp,
                    label = "tileDragElevation",
                )
                Card(
                    modifier = Modifier.clickable { onEdit(tile) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(elevation),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = Color.Gray,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .draggableHandle(),
                        )
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
                            tilePermissionSummary(tile)?.let { summary ->
                                Text(
                                    summary,
                                    fontSize = 11.sp,
                                    color = Color(0xFF5C6BC0),
                                    maxLines = 2,
                                )
                            }
                            Text(
                                "Tap to edit",
                                fontSize = 11.sp,
                                color = Color(0xFF3949AB),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "More options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        showMenu = false
                                        onEdit(tile)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove", color = Color(0xFFD32F2F)) },
                                    onClick = {
                                        showMenu = false
                                        onRemove(tile.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppsTab(
    apps: List<InstalledApp>,
    childAppPackages: Set<String>,
    onAdd: (InstalledApp) -> Unit,
    onRemove: (InstalledApp) -> Unit,
    onLaunch: (InstalledApp) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(apps, key = { it.packageName }) { app ->
            val alreadyAdded = app.packageName in childAppPackages
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (alreadyAdded) onRemove(app) else onAdd(app)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (alreadyAdded) Color(0xFFE8EAF6) else Color.White,
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(app.label, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (alreadyAdded) "On child screen · tap to remove" else "Tap to add to child screen",
                            fontSize = 11.sp,
                            color = Color.Gray,
                        )
                    }
                    IconButton(onClick = { onLaunch(app) }) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = Color(0xFF3949AB),
                        )
                    }
                    if (alreadyAdded) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Remove from child screen",
                            tint = Color(0xFFD32F2F),
                        )
                    } else {
                        Icon(Icons.Default.Add, "Add", tint = Color(0xFF3949AB))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTileEditDialog(
    tile: ChildTile,
    onDismiss: () -> Unit,
    onConfirm: (ChildTile) -> Unit,
) {
    var label by remember(tile.id) { mutableStateOf(tile.label) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit App") },
        text = {
            Column {
                Text(
                    tile.target,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Name on child screen") },
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
                onConfirm(tile.copy(label = label.trim()))
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun tilePermissionSummary(tile: ChildTile): String? {
    if (tile.type == TileType.APP) return null
    val parts = mutableListOf<String>()
    parts += if (tile.webLaunchMode == WebLaunchMode.IN_APP) "In-app browser" else "External browser"
    if (tile.webLaunchMode == WebLaunchMode.IN_APP) {
        val perms = buildList {
            if (tile.cameraPolicy == PermissionPolicy.GRANT) add("camera")
            if (tile.microphonePolicy == PermissionPolicy.GRANT) add("mic")
            if (tile.locationPolicy == PermissionPolicy.GRANT) add("location")
        }
        parts += if (perms.isEmpty()) "No auto-permissions" else "Allows: ${perms.joinToString(", ")}"
    }
    return parts.joinToString(" · ")
}

@Composable
private fun LinkTileDialog(
    existingTile: ChildTile? = null,
    onDismiss: () -> Unit,
    onConfirm: (label: String, url: String, type: TileType, webConfig: WebLinkConfig) -> Unit,
) {
    val isEditing = existingTile != null
    var label by remember(existingTile?.id) { mutableStateOf(existingTile?.label ?: "") }
    var url by remember(existingTile?.id) { mutableStateOf(existingTile?.target ?: "") }
    var linkType by remember(existingTile?.id) {
        mutableIntStateOf(if (existingTile?.type == TileType.YOUTUBE) 1 else 0)
    }
    var useInAppBrowser by remember(existingTile?.id) {
        mutableStateOf(existingTile?.webLaunchMode != WebLaunchMode.EXTERNAL)
    }
    var grantCamera by remember(existingTile?.id) {
        mutableStateOf(existingTile?.cameraPolicy != PermissionPolicy.DENY)
    }
    var grantMicrophone by remember(existingTile?.id) {
        mutableStateOf(existingTile?.microphonePolicy != PermissionPolicy.DENY)
    }
    var grantLocation by remember(existingTile?.id) {
        mutableStateOf(existingTile?.locationPolicy == PermissionPolicy.GRANT)
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Link" else "Add Link") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (!isEditing) {
                    TabRow(selectedTabIndex = linkType) {
                        Tab(selected = linkType == 0, onClick = { linkType = 0 }, text = { Text("Website") })
                        Tab(selected = linkType == 1, onClick = { linkType = 1 }, text = { Text("YouTube") })
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text(
                        if (existingTile?.type == TileType.YOUTUBE) "YouTube video" else "Website",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
                    label = { Text(if (linkType == 1 || existingTile?.type == TileType.YOUTUBE) "YouTube URL" else "Website URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Browser", fontWeight = FontWeight.SemiBold)
                PermissionToggleRow(
                    label = "Open in KidSpace browser (no address bar)",
                    checked = useInAppBrowser,
                    onCheckedChange = { useInAppBrowser = it },
                )
                if (useInAppBrowser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Site permissions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "Applies to this link and other pages on the same domain.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                    PermissionToggleRow("Auto-allow camera", grantCamera) { grantCamera = it }
                    PermissionToggleRow("Auto-allow microphone", grantMicrophone) { grantMicrophone = it }
                    PermissionToggleRow("Auto-allow location", grantLocation) { grantLocation = it }
                }
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
                val type = when {
                    isEditing -> existingTile!!.type
                    linkType == 1 || UrlValidator.isYouTubeUrl(url) -> TileType.YOUTUBE
                    else -> TileType.WEBSITE
                }
                val webConfig = WebLinkConfig(
                    webLaunchMode = if (useInAppBrowser) WebLaunchMode.IN_APP else WebLaunchMode.EXTERNAL,
                    cameraPolicy = if (grantCamera) PermissionPolicy.GRANT else PermissionPolicy.DENY,
                    microphonePolicy = if (grantMicrophone) PermissionPolicy.GRANT else PermissionPolicy.DENY,
                    locationPolicy = if (grantLocation) PermissionPolicy.GRANT else PermissionPolicy.DENY,
                )
                onConfirm(label.trim(), url.trim(), type, webConfig)
            }) {
                Text(if (isEditing) "Save" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun PermissionToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

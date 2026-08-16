package com.kidspace.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kidspace.launcher.BuildConfig
import com.kidspace.launcher.domain.LauncherActions
import com.kidspace.launcher.shortcuts.LauncherShortcutObserver
import com.kidspace.launcher.ui.AppScreen
import com.kidspace.launcher.ui.LauncherLoadingScreen
import com.kidspace.launcher.ui.LauncherViewModel
import com.kidspace.launcher.ui.child.ChildAppearanceScreen
import com.kidspace.launcher.ui.child.ChildHomeScreen
import com.kidspace.launcher.ui.parent.ParentGateScreen
import com.kidspace.launcher.ui.parent.ParentModeScreen
import com.kidspace.launcher.ui.theme.toComposeColor
import com.kidspace.launcher.update.AppUpdateInstaller

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels {
        val app = application as KidSpaceApplication
        LauncherViewModel.Factory(
            app.tileRepository,
            app.appRepository,
            app.appearanceRepository,
            app.parentSettingsRepository,
            app.backupRepository,
            app.appUpdateRepository,
            app.youtubeSearchRepository,
            app.siteIconRepository,
        )
    }

    private var shortcutObserver: LauncherShortcutObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        shortcutObserver = LauncherShortcutObserver(this) {
            viewModel.loadInstalledApps()
        }

        setContent {
            val screen by viewModel.screen.collectAsState()
            val isLauncherReady by viewModel.isLauncherReady.collectAsState()
            val tiles by viewModel.tiles.collectAsState()
            val appearance by viewModel.appearance.collectAsState()
            val parentGate by viewModel.parentGate.collectAsState()
            val installedApps by viewModel.installedApps.collectAsState()
            val backupStatus by viewModel.backupStatus.collectAsState()
            val updateStatus by viewModel.updateStatus.collectAsState()
            val parentSettings by viewModel.parentSettings.collectAsState()
            val youtubeSearch by viewModel.youtubeSearch.collectAsState()
            val showChildAppearance by viewModel.showChildAppearance.collectAsState()
            val childYouTubeVideoIds = tiles
                .filter { it.type == com.kidspace.launcher.data.model.TileType.YOUTUBE }
                .mapNotNull { com.kidspace.launcher.util.YouTubeUtils.extractVideoId(it.target) }
                .toSet()

            val colorScheme = lightColorScheme(
                primary = appearance.primaryColor.toComposeColor(),
                secondary = appearance.secondaryColor.toComposeColor(),
                tertiary = appearance.accentColor.toComposeColor(),
                background = Color(0xFFF5F5F5),
            )

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!isLauncherReady) {
                        LauncherLoadingScreen()
                    } else when (screen) {
                        AppScreen.CHILD -> Box(Modifier.fillMaxSize()) {
                            ChildHomeScreen(
                                tiles = tiles,
                                appearance = appearance,
                                onTileClick = { tile ->
                                    viewModel.launchTile(this@MainActivity, tile)
                                },
                                onAppearanceTileClick = viewModel::openChildAppearance,
                                onParentAccessRequest = viewModel::requestParentAccess,
                            )
                            if (showChildAppearance) {
                                ChildAppearanceScreen(
                                    settings = appearance,
                                    onSave = viewModel::saveAppearance,
                                    onClose = viewModel::closeChildAppearance,
                                )
                            }
                        }
                        AppScreen.PARENT_GATE -> ParentGateScreen(
                            challenge = parentGate.challenge,
                            enteredDigits = parentGate.enteredDigits,
                            errorMessage = parentGate.errorMessage,
                            onDigit = viewModel::onParentDigit,
                            onBackspace = viewModel::onParentBackspace,
                            onSubmit = viewModel::submitParentGate,
                            onCancel = viewModel::cancelParentGate,
                        )
                        AppScreen.PARENT -> ParentModeScreen(
                            tiles = tiles,
                            installedApps = installedApps,
                            appearance = appearance,
                            onExitParentMode = viewModel::exitParentMode,
                            onAddApp = viewModel::addAppTile,
                            onRemoveApp = { app ->
                                viewModel.removeAppFromChildSurface(app.tileTarget())
                            },
                            onLaunchApp = { app ->
                                LauncherActions.launchInstalledApp(this@MainActivity, app)
                            },
                            onAddLink = viewModel::addLinkTile,
                            onUpdateTile = viewModel::updateTile,
                            onRemoveTile = viewModel::removeTile,
                            onReorderTiles = viewModel::reorderTiles,
                            onSaveAppearance = viewModel::saveAppearance,
                            onImportCustomBackground = viewModel::importCustomBackground,
                            onClearCustomBackground = viewModel::clearCustomBackground,
                            onExportBackup = viewModel::exportBackup,
                            onImportBackup = viewModel::importBackup,
                            backupStatusMessage = backupStatus.message,
                            backupStatusIsError = backupStatus.isError,
                            onDismissBackupStatus = viewModel::dismissBackupStatus,
                            appVersionName = BuildConfig.VERSION_NAME,
                            isUpdateDownloading = updateStatus.isDownloading,
                            updateStatusMessage = updateStatus.message,
                            updateStatusIsError = updateStatus.isError,
                            canInstallUpdate = updateStatus.downloadedApk != null,
                            onDownloadUpdate = viewModel::downloadLatestUpdate,
                            onInstallUpdate = {
                                updateStatus.downloadedApk?.let { apk ->
                                    AppUpdateInstaller.install(this@MainActivity, apk)
                                }
                            },
                            onDismissUpdateStatus = viewModel::dismissUpdateStatus,
                            webViewUploadDebugEnabled = parentSettings.webViewUploadDebugEnabled,
                            onWebViewUploadDebugChange = viewModel::setWebViewUploadDebugEnabled,
                            youtubeQuery = youtubeSearch.query,
                            isYouTubeSearching = youtubeSearch.isSearching,
                            youtubeResults = youtubeSearch.results,
                            selectedYouTubeVideoIds = youtubeSearch.selectedVideoIds,
                            childYouTubeVideoIds = childYouTubeVideoIds,
                            youtubeSearchErrorMessage = youtubeSearch.errorMessage,
                            youtubeSearchStatusMessage = youtubeSearch.statusMessage,
                            onYouTubeQueryChange = viewModel::updateYouTubeQuery,
                            onYouTubeSearch = viewModel::searchYouTubeVideos,
                            onToggleYouTubeSelection = viewModel::toggleYouTubeSelection,
                            onSelectAllYouTubeResults = viewModel::selectAllYouTubeResults,
                            onClearYouTubeSelection = viewModel::clearYouTubeSelection,
                            onAddSelectedYouTubeVideos = viewModel::addSelectedYouTubeVideos,
                            onDismissYouTubeSearchStatus = viewModel::dismissYouTubeSearchStatus,
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        shortcutObserver?.start()
        viewModel.loadInstalledApps()
    }

    override fun onStop() {
        shortcutObserver?.stop()
        super.onStop()
    }
}

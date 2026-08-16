package com.kidspace.launcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.InstalledApp
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.data.model.WebLinkConfig
import android.content.Context
import com.kidspace.launcher.data.model.ParentSettings
import com.kidspace.launcher.data.repository.AppearanceRepository
import com.kidspace.launcher.data.repository.AppRepository
import com.kidspace.launcher.data.repository.BackupRepository
import com.kidspace.launcher.data.repository.ParentSettingsRepository
import com.kidspace.launcher.data.repository.TileRepository
import com.kidspace.launcher.domain.LauncherActions
import com.kidspace.launcher.domain.ParentGateChallenge
import com.kidspace.launcher.shortcuts.ShortcutRefreshBus
import com.kidspace.launcher.update.AppUpdateRepository
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.data.model.WebLaunchMode
import com.kidspace.launcher.util.IconKeyGenerator
import com.kidspace.launcher.util.YouTubeUtils
import com.kidspace.launcher.youtube.YouTubeSearchRepository
import com.kidspace.launcher.youtube.YouTubeSearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    CHILD,
    PARENT_GATE,
    PARENT,
}

data class ParentGateUiState(
    val challenge: ParentGateChallenge.Challenge = ParentGateChallenge.generate(),
    val enteredDigits: String = "",
    val errorMessage: String? = null,
)

data class BackupStatus(
    val message: String? = null,
    val isError: Boolean = false,
)

data class AppUpdateStatus(
    val isDownloading: Boolean = false,
    val downloadedApk: File? = null,
    val message: String? = null,
    val isError: Boolean = false,
)

data class YouTubeSearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<YouTubeSearchResult> = emptyList(),
    val selectedVideoIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

class LauncherViewModel(
    private val tileRepository: TileRepository,
    private val appRepository: AppRepository,
    private val appearanceRepository: AppearanceRepository,
    private val parentSettingsRepository: ParentSettingsRepository,
    private val backupRepository: BackupRepository,
    private val appUpdateRepository: AppUpdateRepository,
    private val youtubeSearchRepository: YouTubeSearchRepository,
) : ViewModel() {

    val tiles: StateFlow<List<ChildTile>> = tileRepository.observeTiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appearance: StateFlow<AppearanceSettings> = appearanceRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppearanceSettings())

    val parentSettings: StateFlow<ParentSettings> = parentSettingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ParentSettings())

    private val _screen = MutableStateFlow(AppScreen.CHILD)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    private val _parentGate = MutableStateFlow(ParentGateUiState())
    val parentGate: StateFlow<ParentGateUiState> = _parentGate.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _backupStatus = MutableStateFlow(BackupStatus())
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    private val _showChildAppearance = MutableStateFlow(false)
    val showChildAppearance: StateFlow<Boolean> = _showChildAppearance.asStateFlow()

    private val _isLauncherReady = MutableStateFlow(false)
    val isLauncherReady: StateFlow<Boolean> = _isLauncherReady.asStateFlow()

    private val _updateStatus = MutableStateFlow(AppUpdateStatus())
    val updateStatus: StateFlow<AppUpdateStatus> = _updateStatus.asStateFlow()

    private val _youtubeSearch = MutableStateFlow(YouTubeSearchUiState())
    val youtubeSearch: StateFlow<YouTubeSearchUiState> = _youtubeSearch.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                tileRepository.observeTiles(),
                appearanceRepository.observeSettings(),
            ) { _, _ -> }
                .first()
            _isLauncherReady.value = true
        }
        viewModelScope.launch {
            ShortcutRefreshBus.requests.collect {
                loadInstalledApps()
            }
        }
    }

    fun requestParentAccess() {
        _parentGate.value = ParentGateUiState(challenge = ParentGateChallenge.generate())
        _screen.value = AppScreen.PARENT_GATE
    }

    fun onParentDigit(digit: Int) {
        val current = _parentGate.value
        if (current.enteredDigits.length < current.challenge.expectedDigits.length) {
            _parentGate.value = current.copy(
                enteredDigits = current.enteredDigits + digit,
                errorMessage = null,
            )
        }
    }

    fun onParentBackspace() {
        val current = _parentGate.value
        if (current.enteredDigits.isNotEmpty()) {
            _parentGate.value = current.copy(
                enteredDigits = current.enteredDigits.dropLast(1),
                errorMessage = null,
            )
        }
    }

    fun submitParentGate() {
        val current = _parentGate.value
        if (ParentGateChallenge.verify(current.challenge, current.enteredDigits)) {
            _parentGate.value = ParentGateUiState()
            _screen.value = AppScreen.PARENT
            loadInstalledApps()
        } else {
            _parentGate.value = ParentGateUiState(
                challenge = ParentGateChallenge.generate(),
                errorMessage = "That wasn't quite right. Try again!",
            )
        }
    }

    fun cancelParentGate() {
        _parentGate.value = ParentGateUiState()
        _screen.value = AppScreen.CHILD
    }

    fun exitParentMode() {
        _screen.value = AppScreen.CHILD
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = appRepository.getLaunchableApps()
        }
    }

    fun addAppTile(app: InstalledApp) {
        viewModelScope.launch {
            val target = app.tileTarget()
            val alreadyAdded = tiles.value.any { it.type == TileType.APP && it.target == target }
            if (alreadyAdded) return@launch
            tileRepository.addTile(
                ChildTile(
                    type = TileType.APP,
                    label = app.label,
                    target = target,
                    iconKey = app.tileIconKey(),
                    sortOrder = 0,
                ),
            )
        }
    }

    fun removeAppFromChildSurface(target: String) {
        viewModelScope.launch {
            val tile = tiles.value.find { it.type == TileType.APP && it.target == target }
            if (tile != null) {
                tileRepository.removeTile(tile.id)
            }
        }
    }

    fun toggleAppOnChildSurface(app: InstalledApp) {
        viewModelScope.launch {
            val target = app.tileTarget()
            val existing = tiles.value.find { it.type == TileType.APP && it.target == target }
            if (existing != null) {
                tileRepository.removeTile(existing.id)
            } else {
                tileRepository.addTile(
                    ChildTile(
                        type = TileType.APP,
                        label = app.label,
                        target = target,
                        iconKey = app.tileIconKey(),
                        sortOrder = 0,
                    ),
                )
            }
        }
    }

    fun addLinkTile(label: String, url: String, type: TileType, webConfig: WebLinkConfig) {
        viewModelScope.launch {
            val iconKey = IconKeyGenerator.forUrl(url)
            tileRepository.addTile(
                ChildTile(
                    type = type,
                    label = label,
                    target = url,
                    iconKey = iconKey,
                    sortOrder = 0,
                    webLaunchMode = webConfig.webLaunchMode,
                    cameraPolicy = webConfig.cameraPolicy,
                    microphonePolicy = webConfig.microphonePolicy,
                    locationPolicy = webConfig.locationPolicy,
                    fileUploadPolicy = webConfig.fileUploadPolicy,
                    downloadPolicy = webConfig.downloadPolicy,
                    fullscreenPolicy = webConfig.fullscreenPolicy,
                    cameraCapturePolicy = webConfig.cameraCapturePolicy,
                ),
            )
        }
    }

    fun updateTile(tile: ChildTile) {
        viewModelScope.launch {
            val updated = when (tile.type) {
                TileType.APP -> tile
                TileType.WEBSITE, TileType.YOUTUBE -> tile.copy(
                    iconKey = IconKeyGenerator.forUrl(tile.target),
                )
            }
            tileRepository.updateTile(updated)
        }
    }

    fun removeTile(id: Long) {
        viewModelScope.launch { tileRepository.removeTile(id) }
    }

    fun reorderTiles(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val current = tiles.value.toMutableList()
            if (fromIndex !in current.indices || toIndex !in current.indices) return@launch
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            tileRepository.reorderTiles(current)
        }
    }

    fun saveAppearance(settings: AppearanceSettings) {
        viewModelScope.launch { appearanceRepository.saveSettings(settings) }
    }

    fun importCustomBackground(uri: Uri) {
        viewModelScope.launch { appearanceRepository.importCustomBackground(uri) }
    }

    fun clearCustomBackground() {
        viewModelScope.launch { appearanceRepository.clearCustomBackground() }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupRepository.exportTo(uri) }
                .onSuccess {
                    _backupStatus.value = BackupStatus("Backup exported successfully.")
                }
                .onFailure { error ->
                    _backupStatus.value = BackupStatus(
                        message = "Export failed: ${error.message ?: "Unknown error"}",
                        isError = true,
                    )
                }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { backupRepository.importFrom(uri) }
                .onSuccess {
                    _backupStatus.value = BackupStatus("Backup imported successfully.")
                }
                .onFailure { error ->
                    _backupStatus.value = BackupStatus(
                        message = "Import failed: ${error.message ?: "Unknown error"}",
                        isError = true,
                    )
                }
        }
    }

    fun dismissBackupStatus() {
        _backupStatus.value = BackupStatus()
    }

    fun downloadLatestUpdate() {
        viewModelScope.launch {
            _updateStatus.value = AppUpdateStatus(
                isDownloading = true,
                message = "Downloading latest version…",
            )
            runCatching { appUpdateRepository.downloadLatestApk() }
                .onSuccess { file ->
                    _updateStatus.value = AppUpdateStatus(
                        downloadedApk = file,
                        message = "Download complete. Tap Install update to open the installer.",
                    )
                }
                .onFailure { error ->
                    _updateStatus.value = AppUpdateStatus(
                        message = "Download failed: ${error.message ?: "Unknown error"}",
                        isError = true,
                    )
                }
        }
    }

    fun dismissUpdateStatus() {
        _updateStatus.value = _updateStatus.value.copy(
            message = null,
            isError = false,
            isDownloading = false,
        )
    }

    fun setWebViewUploadDebugEnabled(enabled: Boolean) {
        viewModelScope.launch {
            parentSettingsRepository.setWebViewUploadDebugEnabled(enabled)
        }
    }

    fun launchTile(context: Context, tile: ChildTile) {
        LauncherActions.launchTile(
            context = context,
            tile = tile,
            webViewUploadDebugEnabled = parentSettings.value.webViewUploadDebugEnabled,
        )
    }

    fun updateYouTubeQuery(query: String) {
        _youtubeSearch.value = _youtubeSearch.value.copy(query = query, errorMessage = null)
    }

    fun searchYouTubeVideos() {
        viewModelScope.launch {
            val query = _youtubeSearch.value.query
            _youtubeSearch.value = _youtubeSearch.value.copy(
                isSearching = true,
                errorMessage = null,
                statusMessage = null,
            )
            runCatching { youtubeSearchRepository.search(query) }
                .onSuccess { results ->
                    _youtubeSearch.value = _youtubeSearch.value.copy(
                        isSearching = false,
                        results = results,
                        selectedVideoIds = emptySet(),
                    )
                }
                .onFailure { error ->
                    _youtubeSearch.value = _youtubeSearch.value.copy(
                        isSearching = false,
                        errorMessage = error.message ?: "Search failed",
                    )
                }
        }
    }

    fun toggleYouTubeSelection(videoId: String) {
        val current = _youtubeSearch.value.selectedVideoIds
        _youtubeSearch.value = _youtubeSearch.value.copy(
            selectedVideoIds = if (videoId in current) current - videoId else current + videoId,
        )
    }

    fun selectAllYouTubeResults() {
        val selectable = _youtubeSearch.value.results
            .map { it.videoId }
            .filter { videoId ->
                tiles.value.none { tile ->
                    tile.type == TileType.YOUTUBE &&
                        YouTubeUtils.extractVideoId(tile.target) == videoId
                }
            }
            .toSet()
        _youtubeSearch.value = _youtubeSearch.value.copy(selectedVideoIds = selectable)
    }

    fun clearYouTubeSelection() {
        _youtubeSearch.value = _youtubeSearch.value.copy(selectedVideoIds = emptySet())
    }

    fun addSelectedYouTubeVideos() {
        viewModelScope.launch {
            val selectedIds = _youtubeSearch.value.selectedVideoIds
            val videos = _youtubeSearch.value.results.filter { it.videoId in selectedIds }
            val webConfig = defaultYouTubeWebConfig()
            val toAdd = videos.mapNotNull { video ->
                val exists = tiles.value.any { tile ->
                    tile.type == TileType.YOUTUBE &&
                        YouTubeUtils.extractVideoId(tile.target) == video.videoId
                }
                if (exists) null else {
                    ChildTile(
                        type = TileType.YOUTUBE,
                        label = video.title,
                        target = video.watchUrl,
                        iconKey = IconKeyGenerator.forUrl(video.watchUrl),
                        sortOrder = 0,
                        webLaunchMode = webConfig.webLaunchMode,
                        cameraPolicy = webConfig.cameraPolicy,
                        microphonePolicy = webConfig.microphonePolicy,
                        locationPolicy = webConfig.locationPolicy,
                        fileUploadPolicy = webConfig.fileUploadPolicy,
                        downloadPolicy = webConfig.downloadPolicy,
                        fullscreenPolicy = webConfig.fullscreenPolicy,
                        cameraCapturePolicy = webConfig.cameraCapturePolicy,
                    )
                }
            }
            val added = toAdd.size
            if (added > 0) {
                tileRepository.addTilesAtFront(toAdd)
            }
            _youtubeSearch.value = _youtubeSearch.value.copy(
                selectedVideoIds = emptySet(),
                statusMessage = if (added > 0) {
                    "Added $added video${if (added == 1) "" else "s"} to child screen."
                } else {
                    "Selected videos are already on the child screen."
                },
            )
        }
    }

    fun dismissYouTubeSearchStatus() {
        _youtubeSearch.value = _youtubeSearch.value.copy(
            errorMessage = null,
            statusMessage = null,
        )
    }

    private fun defaultYouTubeWebConfig() = WebLinkConfig(
        webLaunchMode = WebLaunchMode.IN_APP,
        cameraPolicy = PermissionPolicy.GRANT,
        microphonePolicy = PermissionPolicy.GRANT,
        locationPolicy = PermissionPolicy.DENY,
        fileUploadPolicy = PermissionPolicy.DENY,
        downloadPolicy = PermissionPolicy.DENY,
        fullscreenPolicy = PermissionPolicy.GRANT,
        cameraCapturePolicy = PermissionPolicy.DENY,
    )

    fun openChildAppearance() {
        _showChildAppearance.value = true
    }

    fun closeChildAppearance() {
        _showChildAppearance.value = false
    }

    class Factory(
        private val tileRepository: TileRepository,
        private val appRepository: AppRepository,
        private val appearanceRepository: AppearanceRepository,
        private val parentSettingsRepository: ParentSettingsRepository,
        private val backupRepository: BackupRepository,
        private val appUpdateRepository: AppUpdateRepository,
        private val youtubeSearchRepository: YouTubeSearchRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LauncherViewModel(
                tileRepository,
                appRepository,
                appearanceRepository,
                parentSettingsRepository,
                backupRepository,
                appUpdateRepository,
                youtubeSearchRepository,
            ) as T
        }
    }
}

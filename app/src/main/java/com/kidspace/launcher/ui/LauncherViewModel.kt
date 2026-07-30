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
import com.kidspace.launcher.data.repository.AppearanceRepository
import com.kidspace.launcher.data.repository.AppRepository
import com.kidspace.launcher.data.repository.BackupRepository
import com.kidspace.launcher.data.repository.TileRepository
import com.kidspace.launcher.domain.ParentGateChallenge
import com.kidspace.launcher.update.AppUpdateRepository
import com.kidspace.launcher.util.IconKeyGenerator
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

class LauncherViewModel(
    private val tileRepository: TileRepository,
    private val appRepository: AppRepository,
    private val appearanceRepository: AppearanceRepository,
    private val backupRepository: BackupRepository,
    private val appUpdateRepository: AppUpdateRepository,
) : ViewModel() {

    val tiles: StateFlow<List<ChildTile>> = tileRepository.observeTiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appearance: StateFlow<AppearanceSettings> = appearanceRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppearanceSettings())

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

    init {
        viewModelScope.launch {
            combine(
                tileRepository.observeTiles(),
                appearanceRepository.observeSettings(),
            ) { _, _ -> }
                .first()
            _isLauncherReady.value = true
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
            val alreadyAdded = tiles.value.any { it.type == TileType.APP && it.target == app.packageName }
            if (alreadyAdded) return@launch
            tileRepository.addTile(
                ChildTile(
                    type = TileType.APP,
                    label = app.label,
                    target = app.packageName,
                    iconKey = IconKeyGenerator.forApp(app.packageName),
                    sortOrder = 0,
                ),
            )
        }
    }

    fun removeAppFromChildSurface(packageName: String) {
        viewModelScope.launch {
            val tile = tiles.value.find { it.type == TileType.APP && it.target == packageName }
            if (tile != null) {
                tileRepository.removeTile(tile.id)
            }
        }
    }

    fun toggleAppOnChildSurface(app: InstalledApp) {
        viewModelScope.launch {
            val existing = tiles.value.find { it.type == TileType.APP && it.target == app.packageName }
            if (existing != null) {
                tileRepository.removeTile(existing.id)
            } else {
                tileRepository.addTile(
                    ChildTile(
                        type = TileType.APP,
                        label = app.label,
                        target = app.packageName,
                        iconKey = IconKeyGenerator.forApp(app.packageName),
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
                ),
            )
        }
    }

    fun removeTile(id: Long) {
        viewModelScope.launch { tileRepository.removeTile(id) }
    }

    fun moveTile(id: Long, newIndex: Int) {
        viewModelScope.launch {
            val current = tiles.value.toMutableList()
            val oldIndex = current.indexOfFirst { it.id == id }
            if (oldIndex < 0 || newIndex !in current.indices) return@launch
            val item = current.removeAt(oldIndex)
            current.add(newIndex, item)
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
        _updateStatus.value = AppUpdateStatus()
    }

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
        private val backupRepository: BackupRepository,
        private val appUpdateRepository: AppUpdateRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LauncherViewModel(
                tileRepository,
                appRepository,
                appearanceRepository,
                backupRepository,
                appUpdateRepository,
            ) as T
        }
    }
}

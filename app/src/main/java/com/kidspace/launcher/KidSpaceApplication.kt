package com.kidspace.launcher

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.kidspace.launcher.data.db.KidSpaceDatabase
import com.kidspace.launcher.data.repository.AppearanceRepository
import com.kidspace.launcher.data.repository.AppRepository
import com.kidspace.launcher.data.repository.BackupRepository
import com.kidspace.launcher.data.repository.ParentSettingsRepository
import com.kidspace.launcher.data.repository.TileRepository
import com.kidspace.launcher.shortcuts.LegacyShortcutStore
import com.kidspace.launcher.update.AppUpdateRepository
import com.kidspace.launcher.util.SiteIconRepository
import com.kidspace.launcher.youtube.YouTubeSearchRepository

class KidSpaceApplication : Application(), ImageLoaderFactory {
    val database: KidSpaceDatabase by lazy { KidSpaceDatabase.getInstance(this) }
    val tileRepository: TileRepository by lazy { TileRepository(database.childTileDao()) }
    val appRepository: AppRepository by lazy { AppRepository(this, legacyShortcutStore) }
    val legacyShortcutStore: LegacyShortcutStore by lazy { LegacyShortcutStore(this) }
    val appearanceRepository: AppearanceRepository by lazy { AppearanceRepository(this) }
    val parentSettingsRepository: ParentSettingsRepository by lazy { ParentSettingsRepository(this) }
    val backupRepository: BackupRepository by lazy {
        BackupRepository(this, tileRepository, appearanceRepository)
    }
    val appUpdateRepository: AppUpdateRepository by lazy { AppUpdateRepository(this) }
    val youtubeSearchRepository: YouTubeSearchRepository by lazy { YouTubeSearchRepository() }
    val siteIconRepository: SiteIconRepository by lazy { SiteIconRepository() }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(false)
            .build()
    }
}

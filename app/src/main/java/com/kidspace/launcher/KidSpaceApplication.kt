package com.kidspace.launcher

import android.app.Application
import com.kidspace.launcher.data.db.KidSpaceDatabase
import com.kidspace.launcher.data.repository.AppearanceRepository
import com.kidspace.launcher.data.repository.AppRepository
import com.kidspace.launcher.data.repository.BackupRepository
import com.kidspace.launcher.data.repository.TileRepository
import com.kidspace.launcher.update.AppUpdateRepository
import com.kidspace.launcher.youtube.YouTubeSearchRepository

class KidSpaceApplication : Application() {
    val database: KidSpaceDatabase by lazy { KidSpaceDatabase.getInstance(this) }
    val tileRepository: TileRepository by lazy { TileRepository(database.childTileDao()) }
    val appRepository: AppRepository by lazy { AppRepository(this) }
    val appearanceRepository: AppearanceRepository by lazy { AppearanceRepository(this) }
    val backupRepository: BackupRepository by lazy {
        BackupRepository(this, tileRepository, appearanceRepository)
    }
    val appUpdateRepository: AppUpdateRepository by lazy { AppUpdateRepository(this) }
    val youtubeSearchRepository: YouTubeSearchRepository by lazy { YouTubeSearchRepository() }
}

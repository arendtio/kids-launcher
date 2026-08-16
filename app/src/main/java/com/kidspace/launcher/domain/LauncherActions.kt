package com.kidspace.launcher.domain

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import com.kidspace.launcher.KidSpaceApplication
import com.kidspace.launcher.WebViewActivity
import com.kidspace.launcher.YouTubePlayerActivity
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.InstalledApp
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.data.model.WebLaunchMode
import com.kidspace.launcher.shortcuts.ShortcutTarget
import com.kidspace.launcher.util.DomainMatcher
import com.kidspace.launcher.util.YouTubeUtils

object LauncherActions {
    fun launchInstalledApp(context: Context, app: InstalledApp) {
        when {
            app.legacyIntentUri != null -> launchLegacyIntent(context, app.legacyIntentUri)
            app.shortcutId != null && app.shortcutHostPackage != null ->
                launchPinnedShortcut(context, app.shortcutHostPackage, app.shortcutId)
            else -> launchPackage(context, app.packageName)
        }
    }

    fun launchApp(context: Context, target: String) {
        launchAppInternal(context, target)
    }

    fun launchTile(
        context: Context,
        tile: ChildTile,
        webViewUploadDebugEnabled: Boolean = false,
    ) {
        when (tile.type) {
            TileType.APP -> launchAppInternal(context, tile.target)
            TileType.WEBSITE -> launchWebTile(context, tile, webViewUploadDebugEnabled)
            TileType.YOUTUBE -> launchYouTubeTile(context, tile)
        }
    }

    private fun launchAppInternal(context: Context, target: String) {
        when (val ref = ShortcutTarget.decode(target)) {
            is ShortcutTarget.ShortcutRef.Pinned ->
                launchPinnedShortcut(context, ref.hostPackage, ref.shortcutId)
            is ShortcutTarget.ShortcutRef.Legacy -> {
                val store = (context.applicationContext as KidSpaceApplication).legacyShortcutStore
                val intentUri = store.intentUriFor(ref.legacyId) ?: return
                launchLegacyIntent(context, intentUri)
            }
            null -> launchPackage(context, target)
        }
    }

    private fun launchPinnedShortcut(context: Context, hostPackage: String, shortcutId: String) {
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return
        runCatching {
            launcherApps.startShortcut(
                hostPackage,
                shortcutId,
                null,
                null,
                Process.myUserHandle(),
            )
        }
    }

    private fun launchLegacyIntent(context: Context, intentUri: String) {
        runCatching {
            val intent = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun launchPackage(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun launchYouTubeTile(context: Context, tile: ChildTile) {
        if (tile.webLaunchMode == WebLaunchMode.IN_APP) {
            val videoId = YouTubeUtils.extractVideoId(tile.target) ?: run {
                launchExternalUrl(context, tile.target)
                return
            }
            val intent = Intent(context, YouTubePlayerActivity::class.java).apply {
                putExtra(YouTubePlayerActivity.EXTRA_VIDEO_ID, videoId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }
        launchExternalUrl(context, tile.target)
    }

    private fun launchWebTile(
        context: Context,
        tile: ChildTile,
        webViewUploadDebugEnabled: Boolean,
    ) {
        if (tile.webLaunchMode == WebLaunchMode.IN_APP) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.EXTRA_URL, tile.target)
                putExtra(WebViewActivity.EXTRA_CAMERA_POLICY, tile.cameraPolicy.name)
                putExtra(WebViewActivity.EXTRA_MICROPHONE_POLICY, tile.microphonePolicy.name)
                putExtra(WebViewActivity.EXTRA_LOCATION_POLICY, tile.locationPolicy.name)
                putExtra(WebViewActivity.EXTRA_FILE_UPLOAD_POLICY, tile.fileUploadPolicy.name)
                putExtra(WebViewActivity.EXTRA_DOWNLOAD_POLICY, tile.downloadPolicy.name)
                putExtra(WebViewActivity.EXTRA_FULLSCREEN_POLICY, tile.fullscreenPolicy.name)
                putExtra(WebViewActivity.EXTRA_CAMERA_CAPTURE_POLICY, tile.cameraCapturePolicy.name)
                putExtra(WebViewActivity.EXTRA_UPLOAD_DEBUG, webViewUploadDebugEnabled)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            launchExternalUrl(context, tile.target)
        }
    }

    private fun launchExternalUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(DomainMatcher.normalizeUrl(url)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

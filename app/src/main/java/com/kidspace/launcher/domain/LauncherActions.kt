package com.kidspace.launcher.domain

import android.content.Context
import android.content.Intent
import com.kidspace.launcher.WebViewActivity
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.data.model.WebLaunchMode
import com.kidspace.launcher.util.DomainMatcher

object LauncherActions {
    fun launchApp(context: Context, packageName: String) {
        launchAppInternal(context, packageName)
    }

    fun launchTile(context: Context, tile: ChildTile) {
        when (tile.type) {
            TileType.APP -> launchAppInternal(context, tile.target)
            TileType.WEBSITE, TileType.YOUTUBE -> launchWebTile(context, tile)
        }
    }

    private fun launchAppInternal(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun launchWebTile(context: Context, tile: ChildTile) {
        if (tile.webLaunchMode == WebLaunchMode.IN_APP) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.EXTRA_URL, tile.target)
                putExtra(WebViewActivity.EXTRA_CAMERA_POLICY, tile.cameraPolicy.name)
                putExtra(WebViewActivity.EXTRA_MICROPHONE_POLICY, tile.microphonePolicy.name)
                putExtra(WebViewActivity.EXTRA_LOCATION_POLICY, tile.locationPolicy.name)
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

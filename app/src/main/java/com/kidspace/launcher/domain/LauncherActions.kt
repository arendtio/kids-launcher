package com.kidspace.launcher.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kidspace.launcher.data.model.TileType

object LauncherActions {
    fun launchTile(context: Context, type: TileType, target: String) {
        when (type) {
            TileType.APP -> launchApp(context, target)
            TileType.WEBSITE, TileType.YOUTUBE -> launchUrl(context, target)
        }
    }

    private fun launchApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun launchUrl(context: Context, url: String) {
        val normalized = if (url.startsWith("http")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

package com.kidspace.launcher.shortcuts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kidspace.launcher.KidSpaceApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles legacy `com.android.launcher.action.INSTALL_SHORTCUT` broadcasts from browsers
 * that do not use [android.content.pm.ShortcutManager.requestPinShortcut].
 */
class InstallShortcutReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_SHORTCUT) return

        val launchIntent = intent.getParcelableExtraCompat<Intent>(Intent.EXTRA_SHORTCUT_INTENT) ?: return
        val label = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)?.takeIf { it.isNotBlank() } ?: return
        val intentUri = launchIntent.toUri(Intent.URI_INTENT_SCHEME)

        val app = context.applicationContext as KidSpaceApplication
        scope.launch {
            app.legacyShortcutStore.add(label = label, intentUri = intentUri)
            ShortcutRefreshBus.requestRefresh()
        }
    }

    @Suppress("DEPRECATION")
    private inline fun <reified T : android.os.Parcelable> Intent.getParcelableExtraCompat(key: String): T? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            getParcelableExtra(key)
        }
    }

    companion object {
        const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    }
}

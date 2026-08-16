package com.kidspace.launcher.shortcuts

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import androidx.activity.ComponentActivity

/**
 * Listens for pinned shortcut changes while KidSpace is the default launcher.
 */
class LauncherShortcutObserver(
    activity: ComponentActivity,
    private val onShortcutsChanged: () -> Unit,
) {
    private val launcherApps = activity.getSystemService(LauncherApps::class.java)
    private val handler = Handler(Looper.getMainLooper())

    private val callback = object : LauncherApps.Callback() {
        override fun onShortcutsChanged(
            shortcutPackageName: String,
            shortcuts: MutableList<ShortcutInfo>,
            user: UserHandle,
        ) {
            onShortcutsChanged()
        }

        override fun onPackageAdded(packageName: String, user: UserHandle) = Unit

        override fun onPackageChanged(packageName: String, user: UserHandle) = Unit

        override fun onPackageRemoved(packageName: String, user: UserHandle) = Unit

        override fun onPackagesAvailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = Unit

        override fun onPackagesUnavailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = Unit
    }

    fun start() {
        launcherApps?.registerCallback(callback, handler)
    }

    fun stop() {
        launcherApps?.unregisterCallback(callback)
    }
}

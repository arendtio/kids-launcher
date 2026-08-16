package com.kidspace.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.os.Process
import com.kidspace.launcher.data.model.InstalledApp
import com.kidspace.launcher.shortcuts.BrowserShortcutPackages
import com.kidspace.launcher.shortcuts.LegacyShortcutStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val legacyShortcutStore: LegacyShortcutStore,
) {
    suspend fun getLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val apps = queryLauncherActivities()
        val shortcuts = queryPinnedWebShortcuts()
        val legacy = legacyShortcutStore.loadAll().map { it.toInstalledApp() }
        (apps + shortcuts + legacy)
            .distinctBy { it.listKey() }
            .sortedBy { it.label.lowercase() }
    }

    private fun queryLauncherActivities(): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == context.packageName) return@mapNotNull null
                InstalledApp(
                    label = resolveInfo.loadLabel(pm).toString(),
                    packageName = packageName,
                )
            }
            .distinctBy { it.packageName }
    }

    private fun queryPinnedWebShortcuts(): List<InstalledApp> {
        val launcherApps = context.getSystemService(LauncherApps::class.java) ?: return emptyList()
        if (!isDefaultLauncher()) return emptyList()

        val seen = linkedSetOf<String>()
        val shortcuts = mutableListOf<ShortcutInfo>()

        runCatching {
            val query = LauncherApps.ShortcutQuery().apply {
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST,
                )
            }
            shortcuts.addAll(launcherApps.getShortcuts(query, Process.myUserHandle()).orEmpty())
        }

        BrowserShortcutPackages.packageNames.forEach { browserPackage ->
            runCatching {
                val query = LauncherApps.ShortcutQuery().apply {
                    setPackage(browserPackage)
                    setQueryFlags(
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC,
                    )
                }
                shortcuts.addAll(launcherApps.getShortcuts(query, Process.myUserHandle()).orEmpty())
            }
        }

        return shortcuts
            .filter { shortcut -> isRelevantWebShortcut(shortcut) }
            .distinctBy { "${it.`package`}#${it.id}" }
            .mapNotNull { shortcut ->
                val host = shortcut.`package` ?: return@mapNotNull null
                val key = "$host#${shortcut.id}"
                if (!seen.add(key)) return@mapNotNull null
                InstalledApp(
                    label = shortcut.shortLabel?.toString()
                        ?: shortcut.longLabel?.toString()
                        ?: "Shortcut",
                    packageName = host,
                    shortcutHostPackage = host,
                    shortcutId = shortcut.id,
                    browserLabel = BrowserShortcutPackages.labelFor(host) ?: "Browser",
                )
            }
    }

    private fun isRelevantWebShortcut(shortcut: ShortcutInfo): Boolean {
        val hostPackage = shortcut.`package` ?: return false
        if (BrowserShortcutPackages.labelFor(hostPackage) != null) return true
        val launchIntent = shortcut.intent ?: return false
        if (launchIntent.action != Intent.ACTION_VIEW) return false
        val scheme = launchIntent.data?.scheme?.lowercase() ?: return false
        return scheme == "http" || scheme == "https"
    }

    private fun isDefaultLauncher(): Boolean {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == context.packageName
    }
}

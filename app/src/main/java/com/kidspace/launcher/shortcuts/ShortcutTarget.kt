package com.kidspace.launcher.shortcuts

/**
 * Encodes pinned/legacy web shortcuts in [ChildTile.target] for APP tiles.
 *
 * Format: `shortcut:<hostPackage>#<shortcutId>` or `shortcut:legacy#<legacyId>`
 */
object ShortcutTarget {
    private const val PREFIX = "shortcut:"
    private const val LEGACY_HOST = "legacy"

    fun encode(hostPackage: String, shortcutId: String): String = "$PREFIX$hostPackage#$shortcutId"

    fun encodeLegacy(legacyId: String): String = "$PREFIX$LEGACY_HOST#$legacyId"

    fun isShortcutTarget(target: String): Boolean = target.startsWith(PREFIX)

    fun decode(target: String): ShortcutRef? {
        if (!target.startsWith(PREFIX)) return null
        val body = target.removePrefix(PREFIX)
        val separator = body.indexOf('#')
        if (separator <= 0 || separator == body.lastIndex) return null
        val host = body.substring(0, separator)
        val id = body.substring(separator + 1)
        return if (host == LEGACY_HOST) {
            ShortcutRef.Legacy(id)
        } else {
            ShortcutRef.Pinned(host, id)
        }
    }

    sealed class ShortcutRef {
        data class Pinned(val hostPackage: String, val shortcutId: String) : ShortcutRef()
        data class Legacy(val legacyId: String) : ShortcutRef()
    }
}

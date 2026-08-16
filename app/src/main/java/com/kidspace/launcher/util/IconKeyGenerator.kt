package com.kidspace.launcher.util

import com.kidspace.launcher.data.model.RandomIcons
import java.net.URI
import java.security.MessageDigest

object IconKeyGenerator {
    fun forApp(packageName: String): String = "app:$packageName"

    fun forShortcut(hostPackage: String, shortcutId: String): String = "shortcut:$hostPackage#$shortcutId"

    fun forLegacyShortcut(legacyId: String): String = "legacy:$legacyId"

    fun parseShortcutIconKey(iconKey: String): Pair<String, String>? {
        if (!iconKey.startsWith("shortcut:")) return null
        val body = iconKey.removePrefix("shortcut:")
        val separator = body.indexOf('#')
        if (separator <= 0) return null
        return body.substring(0, separator) to body.substring(separator + 1)
    }

    fun forUrl(url: String): String {
        YouTubeUtils.extractVideoId(url)?.let { return "youtube:$it" }
        val host = runCatching { URI(normalizeUrl(url)).host }.getOrNull()
        return if (host != null) "favicon:$host" else randomFor(url)
    }

    fun youtubeThumbnailUrl(iconKey: String): String? {
        if (!iconKey.startsWith("youtube:")) return null
        return YouTubeUtils.thumbnailUrl(iconKey.removePrefix("youtube:"))
    }

    fun randomFor(seed: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray())
        val index = (digest[0].toInt() and 0xFF) % RandomIcons.icons.size
        return "random:${RandomIcons.icons[index]}"
    }

    fun faviconUrl(url: String): String? {
        val host = runCatching { URI(normalizeUrl(url)).host }.getOrNull() ?: return null
        return "https://www.google.com/s2/favicons?domain=$host&sz=128"
    }

    private fun normalizeUrl(url: String): String =
        if (url.startsWith("http")) url else "https://$url"
}

object UrlValidator {
    fun isValidUrl(input: String): Boolean =
        runCatching { URI(if (input.startsWith("http")) input else "https://$input").host }
            .getOrNull()
            ?.isNotBlank() == true

    fun isYouTubeUrl(input: String): Boolean {
        val lower = input.lowercase()
        return lower.contains("youtube.com") || lower.contains("youtu.be")
    }
}

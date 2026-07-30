package com.kidspace.launcher.util

import com.kidspace.launcher.data.model.RandomIcons
import java.net.URI
import java.security.MessageDigest

object IconKeyGenerator {
    fun forApp(packageName: String): String = "app:$packageName"

    fun forUrl(url: String): String {
        val host = runCatching { URI(normalizeUrl(url)).host }.getOrNull()
        return if (host != null) "favicon:$host" else randomFor(url)
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

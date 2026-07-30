package com.kidspace.launcher.util

import java.net.URI

object DomainMatcher {
    fun normalizeUrl(url: String): String =
        if (url.startsWith("http")) url else "https://$url"

    fun hostOf(url: String): String? =
        runCatching { URI(normalizeUrl(url)).host?.lowercase() }.getOrNull()

    fun isAllowedNavigation(startUrl: String, candidateUrl: String): Boolean {
        val allowedHost = hostOf(startUrl) ?: return false
        val candidateHost = hostOf(candidateUrl) ?: return false
        if (candidateHost == allowedHost) return true
        return candidateHost.endsWith(".$allowedHost")
    }
}

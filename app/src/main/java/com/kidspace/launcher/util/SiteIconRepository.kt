package com.kidspace.launcher.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class SiteIconRepository {

    private val cache = ConcurrentHashMap<String, String>()

    suspend fun resolveIconUrl(pageUrl: String): String? = withContext(Dispatchers.IO) {
        val normalized = normalizePageUrl(pageUrl)
        cache[normalized]?.let { return@withContext it }

        val resolved = resolveIconUrlInternal(normalized)
        if (resolved != null) {
            cache[normalized] = resolved
        }
        resolved
    }

    private fun resolveIconUrlInternal(pageUrl: String): String? {
        val html = fetchText(pageUrl) ?: return null
        val candidates = mutableListOf<SiteIconCandidate>()

        candidates += SiteIconResolver.parseHtmlIconCandidates(html, pageUrl)

        val manifestUrls = buildList {
            SiteIconResolver.findManifestUrl(html, pageUrl)?.let { add(it) }
            addAll(SiteIconResolver.guessManifestUrls(pageUrl))
        }.distinct()

        for (manifestUrl in manifestUrls) {
            val manifestJson = fetchText(manifestUrl) ?: continue
            if (!looksLikeManifest(manifestJson)) continue
            candidates += SiteIconResolver.parseManifestIcons(manifestJson, manifestUrl)
            break
        }

        val resolved = SiteIconResolver.selectBestIcon(candidates) ?: return null
        return if (SiteIconResolver.isGoogleFaviconFallback(resolved)) null else resolved
    }

    private fun looksLikeManifest(body: String): Boolean {
        return runCatching { JSONObject(body).has("icons") }.getOrDefault(false)
    }

    private fun fetchText(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        return try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36",
            )
            connection.connect()
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun normalizePageUrl(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }
}

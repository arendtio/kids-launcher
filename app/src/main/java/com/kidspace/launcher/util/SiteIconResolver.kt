package com.kidspace.launcher.util

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

data class SiteIconCandidate(
    val url: String,
    val size: Int,
    val mimeType: String?,
    val purpose: String?,
)

object SiteIconResolver {

    private val manifestLinkRegex = Regex(
        """<link[^>]+rel=["']manifest["'][^>]*href=["']([^"']+)["']""",
        RegexOption.IGNORE_CASE,
    )
    private val linkTagRegex = Regex("""<link\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val hrefRegex = Regex("""href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val relRegex = Regex("""rel=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val sizesRegex = Regex("""sizes=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val typeRegex = Regex("""type=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

    fun findManifestUrl(html: String, pageUrl: String): String? {
        manifestLinkRegex.find(html)?.groupValues?.getOrNull(1)?.let { href ->
            return resolveUrl(pageUrl, href)
        }
        return null
    }

    fun guessManifestUrls(pageUrl: String): List<String> {
        val base = pageUrl.trimEnd('/')
        return listOf(
            "$base/manifest.webmanifest",
            "$base/manifest.json",
            "$base/site.webmanifest",
        )
    }

    fun parseManifestIcons(manifestJson: String, manifestUrl: String): List<SiteIconCandidate> {
        val icons = runCatching { JSONObject(manifestJson).optJSONArray("icons") }.getOrNull()
            ?: return emptyList()
        return parseIconArray(icons, manifestUrl)
    }

    fun parseHtmlIconCandidates(html: String, pageUrl: String): List<SiteIconCandidate> {
        val candidates = mutableListOf<SiteIconCandidate>()
        for (tag in linkTagRegex.findAll(html)) {
            val rel = relRegex.find(tag.value)?.groupValues?.getOrNull(1)?.lowercase() ?: continue
            if (!isIconRel(rel)) continue
            val href = hrefRegex.find(tag.value)?.groupValues?.getOrNull(1) ?: continue
            val sizes = sizesRegex.find(tag.value)?.groupValues?.getOrNull(1)
            val type = typeRegex.find(tag.value)?.groupValues?.getOrNull(1)
            candidates += SiteIconCandidate(
                url = resolveUrl(pageUrl, href),
                size = parseSizeLabel(sizes),
                mimeType = type,
                purpose = if ("apple-touch-icon" in rel) "any" else null,
            )
        }
        return candidates
    }

    fun selectBestIcon(candidates: List<SiteIconCandidate>): String? {
        if (candidates.isEmpty()) return null
        return candidates.maxWithOrNull(
            compareBy<SiteIconCandidate> { it.size }
                .thenBy { mimePriority(it.mimeType) }
                .thenBy { purposePriority(it.purpose) },
        )?.url
    }

    fun googleFaviconFallback(pageUrl: String): String? = IconKeyGenerator.faviconUrl(pageUrl)

    fun isGoogleFaviconFallback(url: String): Boolean {
        return url.contains("google.com/s2/favicons", ignoreCase = true)
    }

    fun isUnresolvedSiteIconKey(iconKey: String): Boolean {
        return iconKey.startsWith("favicon:") || iconKey.startsWith("random:")
    }

    private fun parseIconArray(icons: JSONArray, baseUrl: String): List<SiteIconCandidate> {
        val candidates = mutableListOf<SiteIconCandidate>()
        for (index in 0 until icons.length()) {
            val icon = icons.optJSONObject(index) ?: continue
            val src = icon.optString("src")
            if (src.isBlank()) continue
            candidates += SiteIconCandidate(
                url = resolveUrl(baseUrl, src),
                size = parseSizeLabel(icon.optString("sizes")),
                mimeType = icon.optString("type").takeIf { it.isNotBlank() },
                purpose = icon.optString("purpose").takeIf { it.isNotBlank() },
            )
        }
        return candidates
    }

    private fun isIconRel(rel: String): Boolean {
        val tokens = rel.split(Regex("\\s+"))
        return tokens.any { token ->
            token in setOf("icon", "shortcut", "apple-touch-icon", "apple-touch-icon-precomposed")
        }
    }

    internal fun parseSizeLabel(raw: String?): Int {
        if (raw.isNullOrBlank() || raw.equals("any", ignoreCase = true)) return 0
        val match = Regex("(\\d+)x(\\d+)").find(raw) ?: return 0
        val width = match.groupValues[1].toIntOrNull() ?: 0
        val height = match.groupValues[2].toIntOrNull() ?: 0
        return minOf(width, height)
    }

    internal fun resolveUrl(baseUrl: String, href: String): String {
        if (href.startsWith("http://") || href.startsWith("https://")) return href
        val base = URI(baseUrl)
        return base.resolve(href).toString()
    }

    private fun mimePriority(mimeType: String?): Int = when {
        mimeType == null -> 1
        mimeType.contains("png", ignoreCase = true) -> 4
        mimeType.contains("webp", ignoreCase = true) -> 3
        mimeType.contains("jpeg", ignoreCase = true) || mimeType.contains("jpg", ignoreCase = true) -> 2
        mimeType.contains("svg", ignoreCase = true) -> 0
        else -> 1
    }

    private fun purposePriority(purpose: String?): Int = when {
        purpose == null -> 1
        purpose.contains("any", ignoreCase = true) && !purpose.contains("maskable", ignoreCase = true) -> 2
        purpose.contains("any", ignoreCase = true) -> 1
        purpose.contains("maskable", ignoreCase = true) -> 0
        else -> 1
    }
}

package com.kidspace.launcher.util

import java.net.URI

object YouTubeUtils {
    private val videoIdPattern = Regex("^[\\w-]{11}$")

    fun extractVideoId(url: String): String? {
        val normalized = if (url.startsWith("http")) url else "https://$url"
        val uri = runCatching { URI(normalized) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        val path = uri.path.orEmpty()

        return when {
            host == "youtu.be" -> path.trim('/').split('/').firstOrNull()?.takeIf(::isValidVideoId)
            host.contains("youtube.com") -> extractFromYouTubePath(path, uri.query)
            else -> null
        }
    }

    fun thumbnailUrl(videoId: String, quality: ThumbnailQuality = ThumbnailQuality.HIGH): String =
        "https://img.youtube.com/vi/$videoId/${quality.fileName}"

    fun embedHtml(videoId: String): String {
        val params = buildList {
            add("autoplay=1")
            add("playsinline=1")
            add("rel=0")
            add("modestbranding=1")
            add("controls=1")
            add("fs=1")
            add("iv_load_policy=3")
            add("loop=1")
            add("playlist=$videoId")
            add("enablejsapi=1")
        }.joinToString("&")

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
                <style>
                    html, body {
                        margin: 0;
                        padding: 0;
                        width: 100%;
                        height: 100%;
                        background: #000;
                        overflow: hidden;
                    }
                    iframe {
                        position: fixed;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        border: 0;
                    }
                </style>
            </head>
            <body>
                <iframe
                    src="https://www.youtube.com/embed/$videoId?$params"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                    allowfullscreen>
                </iframe>
            </body>
            </html>
        """.trimIndent()
    }

    fun isYouTubeHost(host: String?): Boolean {
        val lower = host?.lowercase() ?: return false
        return lower == "youtu.be" || lower.contains("youtube.com") || lower.endsWith(".youtube.com")
    }

    fun isAllowedEmbedNavigation(candidateUrl: String): Boolean {
        val host = runCatching {
            URI(if (candidateUrl.startsWith("http")) candidateUrl else "https://$candidateUrl").host?.lowercase()
        }.getOrNull() ?: return false

        return isYouTubeHost(host) ||
            host.endsWith(".googlevideo.com") ||
            host == "googlevideo.com" ||
            host.endsWith(".gstatic.com") ||
            host == "gstatic.com" ||
            host.endsWith(".google.com") ||
            candidateUrl.startsWith("about:blank")
    }

    private fun extractFromYouTubePath(path: String, query: String?): String? {
        val segments = path.trim('/').split('/').filter { it.isNotBlank() }
        if (segments.isEmpty()) return queryParamVideoId(query)

        return when (segments.first()) {
            "watch" -> queryParamVideoId(query)
            "embed", "v", "shorts", "live" -> segments.getOrNull(1)?.takeIf(::isValidVideoId)
            else -> queryParamVideoId(query)
        }
    }

    private fun queryParamVideoId(query: String?): String? {
        if (query.isNullOrBlank()) return null
        return query.split('&')
            .mapNotNull { part ->
                val pieces = part.split('=', limit = 2)
                if (pieces.size == 2 && pieces[0] == "v") pieces[1] else null
            }
            .firstOrNull()
            ?.takeIf(::isValidVideoId)
    }

    private fun isValidVideoId(candidate: String): Boolean = videoIdPattern.matches(candidate)

    enum class ThumbnailQuality(val fileName: String) {
        HIGH("hqdefault.jpg"),
        MAX("maxresdefault.jpg"),
    }
}

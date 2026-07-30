package com.kidspace.launcher.youtube

import org.json.JSONArray
import org.json.JSONObject

object YouTubeSearchResponseParser {
    fun parseInnertubeSearch(json: String, maxResults: Int = 25): List<YouTubeSearchResult> {
        val root = JSONObject(json)
        val videos = mutableListOf<YouTubeSearchResult>()
        val seen = mutableSetOf<String>()
        collectVideoRenderers(root, videos, seen, maxResults)
        return videos
    }

    private fun collectVideoRenderers(
        value: Any?,
        videos: MutableList<YouTubeSearchResult>,
        seen: MutableSet<String>,
        maxResults: Int,
    ) {
        if (videos.size >= maxResults) return
        when (value) {
            is JSONObject -> {
                if (value.has("videoRenderer")) {
                    parseVideoRenderer(value.getJSONObject("videoRenderer"))?.let { result ->
                        if (result.videoId !in seen) {
                            seen.add(result.videoId)
                            videos.add(result)
                        }
                    }
                }
                val keys = value.keys()
                while (keys.hasNext()) {
                    collectVideoRenderers(value.get(keys.next()), videos, seen, maxResults)
                    if (videos.size >= maxResults) return
                }
            }
            is JSONArray -> {
                for (index in 0 until value.length()) {
                    collectVideoRenderers(value.get(index), videos, seen, maxResults)
                    if (videos.size >= maxResults) return
                }
            }
        }
    }

    private fun parseVideoRenderer(renderer: JSONObject): YouTubeSearchResult? {
        val videoId = renderer.optString("videoId")
        if (videoId.isBlank()) return null

        val title = renderer.optJSONObject("title")
            ?.optJSONArray("runs")
            ?.optJSONObject(0)
            ?.optString("text")
            .orEmpty()

        val thumbnailUrl = renderer.optJSONObject("thumbnail")
            ?.optJSONArray("thumbnails")
            ?.let { thumbnails ->
                (thumbnails.length() - 1 downTo 0).firstNotNullOfOrNull { index ->
                    thumbnails.optJSONObject(index)?.optString("url")?.takeIf { it.isNotBlank() }
                }
            }
            .orEmpty()
            .let { url ->
                when {
                    url.startsWith("//") -> "https:$url"
                    else -> url
                }
            }

        val durationLabel = renderer.optJSONObject("lengthText")
            ?.optString("simpleText")
            ?.takeIf { it.isNotBlank() }
            ?: "--:--"

        return YouTubeSearchResult(
            videoId = videoId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            durationLabel = durationLabel,
        )
    }
}

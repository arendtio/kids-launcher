package com.kidspace.launcher.youtube

import org.json.JSONObject

object YouTubeSearchResponseParser {
    fun parseSearchItems(json: String): List<SearchItem> {
        val items = JSONObject(json).optJSONArray("items") ?: return emptyList()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val idObject = item.optJSONObject("id") ?: continue
                val videoId = idObject.optString("videoId")
                if (videoId.isBlank()) continue

                val snippet = item.optJSONObject("snippet") ?: continue
                val title = snippet.optString("title")
                val thumbnails = snippet.optJSONObject("thumbnails")
                val thumbnailUrl = thumbnails
                    ?.optJSONObject("medium")
                    ?.optString("url")
                    ?.takeIf { it.isNotBlank() }
                    ?: thumbnails
                        ?.optJSONObject("default")
                        ?.optString("url")
                        .orEmpty()

                add(SearchItem(videoId = videoId, title = title, thumbnailUrl = thumbnailUrl))
            }
        }
    }

    fun parseDurations(json: String): Map<String, String> {
        val items = JSONObject(json).optJSONArray("items") ?: return emptyMap()
        return buildMap {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val videoId = item.optString("id")
                val duration = item.optJSONObject("contentDetails")?.optString("duration")
                if (videoId.isNotBlank() && !duration.isNullOrBlank()) {
                    put(videoId, YouTubeDurationFormatter.format(duration))
                }
            }
        }
    }

    data class SearchItem(
        val videoId: String,
        val title: String,
        val thumbnailUrl: String,
    )
}

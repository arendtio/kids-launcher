package com.kidspace.launcher.youtube

import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YouTubeSearchRepository {
    suspend fun search(
        query: String,
        apiKey: String,
        maxResults: Int = 25,
    ): List<YouTubeSearchResult> = withContext(Dispatchers.IO) {
        require(query.isNotBlank()) { "Search query is required" }
        require(apiKey.isNotBlank()) { "YouTube API key is required" }

        val encodedQuery = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
        val searchUrl =
            "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet&type=video&safeSearch=strict&maxResults=$maxResults" +
                "&q=$encodedQuery&key=$apiKey"

        val searchJson = fetchJson(searchUrl)
        val searchItems = YouTubeSearchResponseParser.parseSearchItems(searchJson)
        if (searchItems.isEmpty()) return@withContext emptyList()

        val durations = fetchDurations(searchItems.map { it.videoId }, apiKey)
        searchItems.map { item ->
            YouTubeSearchResult(
                videoId = item.videoId,
                title = item.title,
                thumbnailUrl = item.thumbnailUrl,
                durationLabel = durations[item.videoId] ?: "--:--",
            )
        }
    }

    private fun fetchDurations(videoIds: List<String>, apiKey: String): Map<String, String> {
        if (videoIds.isEmpty()) return emptyMap()
        val joinedIds = videoIds.joinToString(",")
        val url =
            "https://www.googleapis.com/youtube/v3/videos" +
                "?part=contentDetails&id=$joinedIds&key=$apiKey"
        val json = fetchJson(url)
        return YouTubeSearchResponseParser.parseDurations(json)
    }

    private fun fetchJson(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 20_000
            requestMethod = "GET"
        }
        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (responseCode !in 200..299) {
                val message = runCatching {
                    org.json.JSONObject(body).optJSONObject("error")?.optString("message")
                }.getOrNull()
                error(message ?: "YouTube API request failed with HTTP $responseCode")
            }
            body
        } finally {
            connection.disconnect()
        }
    }
}

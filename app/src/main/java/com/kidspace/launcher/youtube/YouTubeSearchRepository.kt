package com.kidspace.launcher.youtube

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class YouTubeSearchRepository {
    suspend fun search(
        query: String,
        maxResults: Int = 25,
    ): List<YouTubeSearchResult> = withContext(Dispatchers.IO) {
        require(query.isNotBlank()) { "Search query is required" }
        val json = postInnertubeSearch(query.trim())
        YouTubeSearchResponseParser.parseInnertubeSearch(json, maxResults)
    }

    private fun postInnertubeSearch(query: String): String {
        val connection = (URL(INNERTUBE_SEARCH_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 20_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36",
            )
        }

        val payload = JSONObject().apply {
            put(
                "context",
                JSONObject().apply {
                    put(
                        "client",
                        JSONObject().apply {
                            put("clientName", "WEB")
                            put("clientVersion", CLIENT_VERSION)
                            put("hl", "en")
                            put("gl", "US")
                        },
                    )
                },
            )
            put("query", query)
        }.toString()

        return try {
            connection.outputStream.use { stream ->
                stream.write(payload.toByteArray(Charsets.UTF_8))
            }
            val responseCode = connection.responseCode
            val body = (if (responseCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            if (responseCode !in 200..299) {
                error("YouTube search failed with HTTP $responseCode")
            }
            body
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val INNERTUBE_SEARCH_URL =
            "https://www.youtube.com/youtubei/v1/search?prettyPrint=false"
        private const val CLIENT_VERSION = "2.20240101.00.00"
    }
}

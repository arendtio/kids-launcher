package com.kidspace.launcher.youtube

import org.junit.Assert.assertEquals
import org.junit.Test

class YouTubeSearchResponseParserTest {

    @Test
    fun `parseSearchItems extracts video metadata`() {
        val json = """
            {
              "items": [
                {
                  "id": { "videoId": "abc123def45" },
                  "snippet": {
                    "title": "Fun video",
                    "thumbnails": {
                      "medium": { "url": "https://img.youtube.com/vi/abc123def45/hqdefault.jpg" }
                    }
                  }
                }
              ]
            }
        """.trimIndent()

        val items = YouTubeSearchResponseParser.parseSearchItems(json)
        assertEquals(1, items.size)
        assertEquals("abc123def45", items[0].videoId)
        assertEquals("Fun video", items[0].title)
    }

    @Test
    fun `parseDurations maps video ids to formatted labels`() {
        val json = """
            {
              "items": [
                {
                  "id": "abc123def45",
                  "contentDetails": { "duration": "PT4M13S" }
                }
              ]
            }
        """.trimIndent()

        val durations = YouTubeSearchResponseParser.parseDurations(json)
        assertEquals("4:13", durations["abc123def45"])
    }
}

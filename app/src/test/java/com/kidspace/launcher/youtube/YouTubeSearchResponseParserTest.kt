package com.kidspace.launcher.youtube

import org.junit.Assert.assertEquals
import org.junit.Test

class YouTubeSearchResponseParserTest {

    @Test
    fun `parseInnertubeSearch extracts video metadata`() {
        val json = """
            {
              "contents": {
                "twoColumnSearchResultsRenderer": {
                  "primaryContents": {
                    "sectionListRenderer": {
                      "contents": [
                        {
                          "itemSectionRenderer": {
                            "contents": [
                              {
                                "videoRenderer": {
                                  "videoId": "abc123def45",
                                  "title": {
                                    "runs": [{ "text": "Fun video" }]
                                  },
                                  "thumbnail": {
                                    "thumbnails": [
                                      { "url": "https://i.ytimg.com/vi/abc123def45/hqdefault.jpg" }
                                    ]
                                  },
                                  "lengthText": { "simpleText": "4:13" }
                                }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val results = YouTubeSearchResponseParser.parseInnertubeSearch(json)
        assertEquals(1, results.size)
        assertEquals("abc123def45", results[0].videoId)
        assertEquals("Fun video", results[0].title)
        assertEquals("4:13", results[0].durationLabel)
    }

    @Test
    fun `parseInnertubeSearch deduplicates repeated video renderers`() {
        val json = """
            {
              "videoRenderer": {
                "videoId": "abc123def45",
                "title": { "runs": [{ "text": "Fun video" }] },
                "thumbnail": { "thumbnails": [{ "url": "https://i.ytimg.com/vi/abc123def45/hqdefault.jpg" }] },
                "lengthText": { "simpleText": "4:13" }
              },
              "nested": {
                "videoRenderer": {
                  "videoId": "abc123def45",
                  "title": { "runs": [{ "text": "Fun video" }] },
                  "thumbnail": { "thumbnails": [{ "url": "https://i.ytimg.com/vi/abc123def45/hqdefault.jpg" }] },
                  "lengthText": { "simpleText": "4:13" }
                }
              }
            }
        """.trimIndent()

        val results = YouTubeSearchResponseParser.parseInnertubeSearch(json)
        assertEquals(1, results.size)
    }
}

package com.kidspace.launcher.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeUtilsTest {

    @Test
    fun `extractVideoId from watch url`() {
        assertEquals("dQw4w9WgXcQ", YouTubeUtils.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
    }

    @Test
    fun `extractVideoId from youtu be url`() {
        assertEquals("dQw4w9WgXcQ", YouTubeUtils.extractVideoId("https://youtu.be/dQw4w9WgXcQ"))
    }

    @Test
    fun `extractVideoId from shorts url`() {
        assertEquals("abc123def45", YouTubeUtils.extractVideoId("https://www.youtube.com/shorts/abc123def45"))
    }

    @Test
    fun `extractVideoId from embed url`() {
        assertEquals("abc123def45", YouTubeUtils.extractVideoId("https://www.youtube.com/embed/abc123def45"))
    }

    @Test
    fun `extractVideoId returns null for non youtube url`() {
        assertNull(YouTubeUtils.extractVideoId("https://example.com/watch?v=dQw4w9WgXcQ"))
    }

    @Test
    fun `thumbnailUrl uses hqdefault`() {
        assertEquals(
            "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            YouTubeUtils.thumbnailUrl("dQw4w9WgXcQ"),
        )
    }

    @Test
    fun `playerHtml uses iframe api and returns home on end`() {
        val origin = "https://com.kidspace.launcher"
        val html = YouTubeUtils.playerHtml("dQw4w9WgXcQ", origin)
        assertTrue(html.contains("iframe_api"))
        assertTrue(html.contains("onYouTubeIframeAPIReady"))
        assertTrue(html.contains("videoId: 'dQw4w9WgXcQ'"))
        assertTrue(html.contains("KidSpacePlayer.onVideoEnded()"))
        assertTrue(html.contains("\"origin\": \"$origin\""))
        assertFalse(html.contains("loop=1"))
        assertFalse(html.contains("playlist="))
    }

    @Test
    fun `embedUrl includes origin parameter`() {
        val url = YouTubeUtils.embedUrl("dQw4w9WgXcQ", "https://com.kidspace.launcher")
        assertTrue(url.contains("origin=https%3A%2F%2Fcom.kidspace.launcher"))
    }

    @Test
    fun `embedHeaders include referer`() {
        val headers = YouTubeUtils.embedHeaders("https://com.kidspace.launcher")
        assertEquals("https://com.kidspace.launcher", headers["Referer"])
        assertEquals("strict-origin-when-cross-origin", headers["Referrer-Policy"])
    }

    @Test
    fun `isAllowedEmbedNavigation allows youtube and googlevideo`() {
        assertTrue(YouTubeUtils.isAllowedEmbedNavigation("https://www.youtube.com/embed/abc"))
        assertTrue(YouTubeUtils.isAllowedEmbedNavigation("https://r1---sn-abc.googlevideo.com/videoplayback"))
        assertFalse(YouTubeUtils.isAllowedEmbedNavigation("https://example.com"))
    }
}

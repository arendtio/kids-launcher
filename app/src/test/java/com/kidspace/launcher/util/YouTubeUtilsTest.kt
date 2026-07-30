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
    fun `embedHtml includes loop and playlist`() {
        val html = YouTubeUtils.embedHtml("dQw4w9WgXcQ")
        assertTrue(html.contains("youtube.com/embed/dQw4w9WgXcQ"))
        assertTrue(html.contains("loop=1"))
        assertTrue(html.contains("playlist=dQw4w9WgXcQ"))
        assertTrue(html.contains("rel=0"))
    }

    @Test
    fun `isAllowedEmbedNavigation allows youtube and googlevideo`() {
        assertTrue(YouTubeUtils.isAllowedEmbedNavigation("https://www.youtube.com/embed/abc"))
        assertTrue(YouTubeUtils.isAllowedEmbedNavigation("https://r1---sn-abc.googlevideo.com/videoplayback"))
        assertFalse(YouTubeUtils.isAllowedEmbedNavigation("https://example.com"))
    }
}

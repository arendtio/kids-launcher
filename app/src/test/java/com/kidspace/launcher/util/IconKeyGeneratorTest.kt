package com.kidspace.launcher.util

import org.junit.Assert.assertEquals
import org.junit.Test

class IconKeyGeneratorTest {

    @Test
    fun `forUrl uses youtube thumbnail key for youtube links`() {
        assertEquals(
            "youtube:dQw4w9WgXcQ",
            IconKeyGenerator.forUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
        )
    }

    @Test
    fun `forUrl uses favicon for regular websites`() {
        assertEquals(
            "favicon:pbskids.org",
            IconKeyGenerator.forUrl("https://pbskids.org/games"),
        )
    }

    @Test
    fun `youtubeThumbnailUrl resolves icon key`() {
        assertEquals(
            "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
            IconKeyGenerator.youtubeThumbnailUrl("youtube:dQw4w9WgXcQ"),
        )
    }
}

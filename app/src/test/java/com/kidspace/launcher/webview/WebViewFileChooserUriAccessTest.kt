package com.kidspace.launcher.webview

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewFileChooserUriAccessTest {
    @Test
    fun normalizeAcceptTypes_emptyUsesWildcard() {
        assertArrayEquals(arrayOf("*/*"), WebViewFileChooserUriAccess.normalizeAcceptTypes(null))
        assertArrayEquals(arrayOf("*/*"), WebViewFileChooserUriAccess.normalizeAcceptTypes(emptyArray()))
    }

    @Test
    fun normalizeAcceptTypes_mapsZipExtensionAndMime() {
        val normalized = WebViewFileChooserUriAccess.normalizeAcceptTypes(
            arrayOf(".zip", "application/zip"),
        )
        assertTrue(normalized.contains("application/zip"))
        assertTrue(normalized.contains("application/octet-stream"))
    }

    @Test
    fun normalizeAcceptTypes_keepsStandardMimeTypes() {
        assertArrayEquals(
            arrayOf("image/*", "application/pdf"),
            WebViewFileChooserUriAccess.normalizeAcceptTypes(
                arrayOf("image/*", "", "application/pdf"),
            ),
        )
    }

    @Test
    fun normalizeAcceptTypes_mapsBareExtension() {
        val normalized = WebViewFileChooserUriAccess.normalizeAcceptTypes(arrayOf("json"))
        assertEquals("application/json", normalized.single())
    }
}

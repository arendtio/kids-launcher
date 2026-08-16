package com.kidspace.launcher.webview

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebViewImportSessionTest {
    @Before
    fun setUp() {
        WebViewImportSession.markImportFinished()
        WebViewFileChooserCallbackStore.clear()
        WebViewFileChooserCallbackStore.markRecentFileDelivery()
    }

    @Test
    fun markImportStartedWhenRecentFileDelivery() {
        WebViewFileChooserCallbackStore.markRecentFileDelivery()
        WebViewImportSession.markImportStartedIfRecentFileUpload()
        assertTrue(WebViewImportSession.importInFlight)
        assertTrue(WebViewImportSession.shouldRetainWebView())
    }

    @Test
    fun markImportFinishedClearsGuard() {
        WebViewImportSession.markImportStartedIfRecentFileUpload()
        WebViewImportSession.markImportFinished()
        assertFalse(WebViewImportSession.importInFlight)
    }
}

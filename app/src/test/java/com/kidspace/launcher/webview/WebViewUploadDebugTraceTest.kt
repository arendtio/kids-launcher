package com.kidspace.launcher.webview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewUploadDebugTraceTest {
    @Test
    fun disabledTraceDoesNotRecordEvents() {
        val trace = WebViewUploadDebugTrace(enabled = false)
        trace.event("should not appear")
        assertEquals("Upload-Debug aktiv. Wähle eine Datei …", trace.render())
    }

    @Test
    fun enabledTraceRecordsEvents() {
        val updates = mutableListOf<String>()
        val trace = WebViewUploadDebugTrace(enabled = true) { updates.add(it) }
        trace.event("picker opened")
        assertTrue(trace.render().contains("picker opened"))
        assertTrue(trace.shareText().contains("picker opened"))
        assertTrue(updates.isNotEmpty())
    }
}

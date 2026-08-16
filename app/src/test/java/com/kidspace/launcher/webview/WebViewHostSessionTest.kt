package com.kidspace.launcher.webview

import android.net.Uri
import android.webkit.ValueCallback
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebViewHostSessionTest {

    @Before
    fun setUp() {
        WebViewHostSession.resetForTests()
    }

    @Test
    fun pendingFileCallback_marksExternalUi() {
        val callback = ValueCallback<Array<Uri>> { }
        WebViewHostSession.setPendingFileCallback(callback)
        assertTrue(WebViewHostSession.isExternalUiActive)
        assertTrue(WebViewHostSession.shouldRetainWebView)
        assertSame(callback, WebViewHostSession.peekPendingFileCallback())
    }

    @Test
    fun takePendingFileCallback_clearsStoredCallback() {
        val callback = ValueCallback<Array<Uri>> { }
        WebViewHostSession.setPendingFileCallback(callback)
        assertSame(callback, WebViewHostSession.takePendingFileCallback())
        assertNull(WebViewHostSession.peekPendingFileCallback())
        assertTrue(WebViewHostSession.isExternalUiActive)
    }

    @Test
    fun externalUiFinishedWithFiles_entersJsHandoff() {
        WebViewHostSession.setPendingFileCallback(ValueCallback { })
        WebViewHostSession.onExternalUiFinished(withFiles = true)
        assertFalse(WebViewHostSession.isExternalUiActive)
        assertFalse(WebViewHostSession.shouldRetainWebView)
        assertTrue(WebViewHostSession.phase == WebViewHostSession.Phase.JS_HANDOFF)
    }

    @Test
    fun jsConfirmAfterHandoff_awaitsNavigation() {
        WebViewHostSession.onExternalUiFinished(withFiles = true)
        WebViewHostSession.onJsConfirmAccepted()
        assertTrue(WebViewHostSession.isAwaitingNavigation)
        assertTrue(WebViewHostSession.shouldRetainWebView)
    }

    @Test
    fun navigationFinished_clearsAwaitingNavigation() {
        WebViewHostSession.onExternalUiFinished(withFiles = true)
        WebViewHostSession.onJsConfirmAccepted()
        WebViewHostSession.onNavigationFinished()
        assertFalse(WebViewHostSession.isAwaitingNavigation)
        assertFalse(WebViewHostSession.shouldRetainWebView)
    }

    @Test
    fun externalUiFinishedWithoutFiles_returnsToActive() {
        WebViewHostSession.setPendingFileCallback(ValueCallback { })
        WebViewHostSession.onExternalUiFinished(withFiles = false)
        assertTrue(WebViewHostSession.phase == WebViewHostSession.Phase.ACTIVE)
        assertFalse(WebViewHostSession.shouldRetainWebView)
    }

    @Test
    fun jsHandoffCancelled_returnsToActive() {
        WebViewHostSession.onExternalUiFinished(withFiles = true)
        WebViewHostSession.onJsHandoffCancelled()
        assertTrue(WebViewHostSession.phase == WebViewHostSession.Phase.ACTIVE)
    }
}

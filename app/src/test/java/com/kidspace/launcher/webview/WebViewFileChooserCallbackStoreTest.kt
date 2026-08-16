package com.kidspace.launcher.webview

import android.net.Uri
import android.webkit.ValueCallback
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebViewFileChooserCallbackStoreTest {
    @Before
    fun setUp() {
        WebViewFileChooserCallbackStore.clear()
    }

    @Test
    fun setPending_marksPickerInFlight() {
        val callback = ValueCallback<Array<Uri>> { }
        WebViewFileChooserCallbackStore.setPending(callback)
        assertTrue(WebViewFileChooserCallbackStore.pickerInFlight)
        assertSame(callback, WebViewFileChooserCallbackStore.peek())
    }

    @Test
    fun take_clearsStoredCallback() {
        val callback = ValueCallback<Array<Uri>> { }
        WebViewFileChooserCallbackStore.setPending(callback)
        assertSame(callback, WebViewFileChooserCallbackStore.take())
        assertNull(WebViewFileChooserCallbackStore.peek())
        assertFalse(WebViewFileChooserCallbackStore.pickerInFlight)
    }
}

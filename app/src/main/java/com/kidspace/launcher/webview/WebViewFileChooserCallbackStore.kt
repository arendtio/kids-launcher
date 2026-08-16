package com.kidspace.launcher.webview

import android.net.Uri
import android.webkit.ValueCallback

/**
 * Retains the WebView file-chooser callback across [android.app.Activity] recreation while the
 * system picker is open. Without this, [android.webkit.WebChromeClient.onShowFileChooser]'s
 * callback is lost and [android.webkit.ValueCallback.onReceiveValue] is never called.
 */
object WebViewFileChooserCallbackStore {
    private var callback: ValueCallback<Array<Uri>>? = null

    var pickerInFlight: Boolean = false
        private set

    var recentFileDelivery: Boolean = false
        private set

    fun markRecentFileDelivery() {
        recentFileDelivery = true
    }

    fun hasRecentFileDelivery(): Boolean = recentFileDelivery

    fun clearRecentFileDelivery() {
        recentFileDelivery = false
    }

    fun setPending(value: ValueCallback<Array<Uri>>?) {
        callback = value
        pickerInFlight = value != null
    }

    fun peek(): ValueCallback<Array<Uri>>? = callback

    fun take(): ValueCallback<Array<Uri>>? {
        val value = callback
        callback = null
        pickerInFlight = false
        return value
    }

    fun clear() {
        callback = null
        pickerInFlight = false
    }
}

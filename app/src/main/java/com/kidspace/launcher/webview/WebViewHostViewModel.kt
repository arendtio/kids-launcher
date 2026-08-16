package com.kidspace.launcher.webview

import android.webkit.WebView
import androidx.lifecycle.ViewModel

/** Keeps the [WebView] instance across [android.app.Activity] recreation (e.g. returning from the file picker). */
class WebViewHostViewModel : ViewModel() {
    var webView: WebView? = null

    override fun onCleared() {
        webView?.destroy()
        webView = null
        super.onCleared()
    }
}

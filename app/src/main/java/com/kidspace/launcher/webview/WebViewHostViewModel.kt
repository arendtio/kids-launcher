package com.kidspace.launcher.webview

import android.webkit.WebView
import androidx.lifecycle.ViewModel

/** Keeps the [WebView] instance across configuration changes when the file picker is not open. */
class WebViewHostViewModel : ViewModel() {
    var webView: WebView? = null

    override fun onCleared() {
        val current = webView
        webView = null
        if (current != null && WebViewFileChooserCallbackStore.pickerInFlight) {
            WebViewUploadSession.retain(current)
        } else {
            current?.destroy()
        }
        super.onCleared()
    }
}

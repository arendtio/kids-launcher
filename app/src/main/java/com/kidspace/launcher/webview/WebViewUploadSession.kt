package com.kidspace.launcher.webview

import android.view.ViewGroup
import android.webkit.WebView

/**
 * Retains the active [WebView] while the system file picker is open and the hosting
 * [android.app.Activity] may finish or recreate (which clears a per-activity [androidx.lifecycle.ViewModel]).
 */
object WebViewUploadSession {
    private var retainedWebView: WebView? = null

    fun bind(webView: WebView) {
        retainedWebView = webView
    }

    fun activeWebView(): WebView? = retainedWebView

    fun retain(webView: WebView) {
        (webView.parent as? ViewGroup)?.removeView(webView)
        retainedWebView = webView
    }

    fun clear() {
        retainedWebView?.destroy()
        retainedWebView = null
    }
}

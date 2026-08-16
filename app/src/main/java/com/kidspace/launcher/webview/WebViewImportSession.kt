package com.kidspace.launcher.webview

/** Tracks async backup import after a file upload + JS confirm so the WebView survives until reload. */
object WebViewImportSession {
    var importInFlight: Boolean = false
        private set

    fun markImportStartedIfRecentFileUpload() {
        if (WebViewFileChooserCallbackStore.hasRecentFileDelivery()) {
            importInFlight = true
            WebViewFileChooserCallbackStore.clearRecentFileDelivery()
        }
    }

    fun markImportFinished() {
        importInFlight = false
    }

    fun shouldRetainWebView(): Boolean {
        return importInFlight || WebViewFileChooserCallbackStore.pickerInFlight
    }
}

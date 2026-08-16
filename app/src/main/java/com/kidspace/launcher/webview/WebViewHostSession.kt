package com.kidspace.launcher.webview

import android.net.Uri
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebView

/**
 * Coordinates WebView survival and pending Chrome callbacks while external UI
 * temporarily takes focus from the in-app browser (file picker, camera, JS confirm, reload).
 *
 * Replaces separate upload/import/callback-retention singletons with one explicit lifecycle.
 */
object WebViewHostSession {

    enum class Phase {
        /** Normal in-app browsing. */
        ACTIVE,
        /** System UI has focus; file-chooser callback must survive activity recreation. */
        EXTERNAL_UI,
        /** File was delivered; JS may run confirm/import logic before navigation. */
        JS_HANDOFF,
        /** User confirmed; waiting for the page to reload or navigate. */
        AWAITING_NAVIGATION,
    }

    var phase: Phase = Phase.ACTIVE
        private set

    private var retainedWebView: WebView? = null
    private var pendingFileCallback: ValueCallback<Array<Uri>>? = null

    val shouldRetainWebView: Boolean
        get() = phase == Phase.EXTERNAL_UI || phase == Phase.AWAITING_NAVIGATION

    val shouldSkipRestoreState: Boolean
        get() = phase != Phase.ACTIVE || retainedWebView != null

    val isExternalUiActive: Boolean
        get() = phase == Phase.EXTERNAL_UI

    val isAwaitingNavigation: Boolean
        get() = phase == Phase.AWAITING_NAVIGATION

    fun bind(webView: WebView) {
        retainedWebView = webView
    }

    fun activeWebView(): WebView? = retainedWebView

    fun retain(webView: WebView) {
        (webView.parent as? ViewGroup)?.removeView(webView)
        retainedWebView = webView
    }

    fun setPendingFileCallback(callback: ValueCallback<Array<Uri>>?) {
        pendingFileCallback = callback
        if (callback != null) {
            phase = Phase.EXTERNAL_UI
        }
    }

    fun peekPendingFileCallback(): ValueCallback<Array<Uri>>? = pendingFileCallback

    fun takePendingFileCallback(): ValueCallback<Array<Uri>>? {
        val callback = pendingFileCallback
        pendingFileCallback = null
        return callback
    }

    fun clearPendingFileCallback() {
        pendingFileCallback = null
        if (phase == Phase.EXTERNAL_UI) {
            phase = Phase.ACTIVE
        }
    }

    /** Picker/camera finished; hand results back to the page or return to normal browsing. */
    fun onExternalUiFinished(withFiles: Boolean) {
        pendingFileCallback = null
        phase = if (withFiles) Phase.JS_HANDOFF else Phase.ACTIVE
    }

    /** JS confirm accepted while a file handoff is in progress (e.g. backup import). */
    fun onJsConfirmAccepted() {
        if (phase == Phase.JS_HANDOFF) {
            phase = Phase.AWAITING_NAVIGATION
        }
    }

    /** JS confirm dismissed while a file handoff is in progress. */
    fun onJsHandoffCancelled() {
        if (phase == Phase.JS_HANDOFF) {
            phase = Phase.ACTIVE
        }
    }

    /** Page finished loading after a confirmed import/navigation flow. */
    fun onNavigationFinished() {
        if (phase == Phase.AWAITING_NAVIGATION) {
            phase = Phase.ACTIVE
        }
    }

    /** Host cancelled pending work without delivering a result. */
    fun onHostCancelled() {
        pendingFileCallback = null
        phase = Phase.ACTIVE
    }

    /** Test-only reset. */
    internal fun resetForTests() {
        phase = Phase.ACTIVE
        pendingFileCallback = null
        retainedWebView?.destroy()
        retainedWebView = null
    }
}

package com.kidspace.launcher.webview

/**
 * Defers work until the WebView host activity has finished its resume sequence
 * ([android.webkit.WebView.onResume] called before [markReady]).
 *
 * Activity-result callbacks (file picker) fire during [android.app.Activity.onResume]
 * before [WebView.onResume], so JS triggered from those callbacks must wait for [markReady].
 */
class WebViewHostResumeGate(
    private val isHostValid: () -> Boolean = { true },
) {
    private val pendingActions = ArrayDeque<() -> Unit>()

    var isReady: Boolean = false
        private set

    fun markReady() {
        isReady = true
        flushPending()
    }

    fun markNotReady() {
        isReady = false
    }

    fun runWhenReady(action: () -> Unit) {
        if (isReady && isHostValid()) {
            action()
        } else {
            pendingActions.addLast(action)
        }
    }

    fun clearPending() {
        pendingActions.clear()
    }

    val hasPendingActions: Boolean
        get() = pendingActions.isNotEmpty()

    val pendingActionCount: Int
        get() = pendingActions.size

    private fun flushPending() {
        if (!isReady || !isHostValid()) return
        while (pendingActions.isNotEmpty()) {
            pendingActions.removeFirst().invoke()
        }
    }
}

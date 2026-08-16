package com.kidspace.launcher.webview

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Traces WebView file-upload and JS-dialog steps for troubleshooting.
 *
 * When enabled, events are written to Logcat ([LOG_TAG]) and shown in an on-screen overlay.
 */
class WebViewUploadDebugTrace(
    private val enabled: Boolean,
    private val onUpdate: ((String) -> Unit)? = null,
) {
    private val lines = ArrayDeque<String>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun event(message: String) {
        if (!enabled) return
        val line = "${timeFormat.format(Date())}  $message"
        runCatching { Log.i(LOG_TAG, message) }
        synchronized(lines) {
            lines.addLast(line)
            while (lines.size > MAX_LINES) {
                lines.removeFirst()
            }
        }
        onUpdate?.invoke(render())
    }

    fun render(): String = synchronized(lines) {
        if (lines.isEmpty()) {
            "Upload-Debug aktiv. Wähle eine Datei …"
        } else {
            lines.joinToString("\n")
        }
    }

    fun shareText(): String = buildString {
        appendLine("KidSpace WebView Upload Debug")
        appendLine("---")
        append(render())
    }

    companion object {
        const val LOG_TAG = "KidSpaceFileUpload"
        private const val MAX_LINES = 30
    }
}

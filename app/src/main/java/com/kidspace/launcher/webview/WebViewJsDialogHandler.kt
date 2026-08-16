package com.kidspace.launcher.webview

import android.app.AlertDialog
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebView
import android.widget.EditText
import androidx.activity.ComponentActivity

/**
 * Handles JavaScript dialog APIs in the in-app browser:
 * [android.webkit.WebChromeClient.onJsAlert],
 * [android.webkit.WebChromeClient.onJsConfirm],
 * [android.webkit.WebChromeClient.onJsPrompt], and
 * [android.webkit.WebChromeClient.onJsBeforeUnload].
 */
class WebViewJsDialogHandler(
    private val activity: ComponentActivity,
) {
    fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (result == null) return false
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                result.cancel()
                return@runOnUiThread
            }
            AlertDialog.Builder(activity)
                .setMessage(message.orEmpty())
                .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                .setOnCancelListener { result.cancel() }
                .show()
        }
        return true
    }

    fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (result == null) return false
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                result.cancel()
                return@runOnUiThread
            }
            AlertDialog.Builder(activity)
                .setMessage(message.orEmpty())
                .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
                .setOnCancelListener { result.cancel() }
                .show()
        }
        return true
    }

    fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?,
    ): Boolean {
        if (result == null) return false
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                result.cancel()
                return@runOnUiThread
            }
            val input = EditText(activity).apply {
                setText(defaultValue.orEmpty())
            }
            AlertDialog.Builder(activity)
                .setMessage(message.orEmpty())
                .setView(input)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    result.confirm(input.text.toString())
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
                .setOnCancelListener { result.cancel() }
                .show()
        }
        return true
    }

    fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (result == null) return false
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                result.cancel()
                return@runOnUiThread
            }
            AlertDialog.Builder(activity)
                .setMessage(message?.takeIf { it.isNotBlank() } ?: "Leave this page?")
                .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
                .setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
                .setOnCancelListener { result.cancel() }
                .show()
        }
        return true
    }
}

package com.kidspace.launcher.webview

import android.app.AlertDialog
import android.view.ContextThemeWrapper
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebView
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles JavaScript dialog APIs in the in-app browser.
 *
 * Dialogs triggered immediately after the system file picker closes (e.g. `confirm()` in an
 * `<input type="file">` change handler) can arrive before the activity is resumed; those requests
 * are queued and shown on the next resume.
 */
class WebViewJsDialogHandler(
    private val activity: ComponentActivity,
) {
    private var activeDialog: AlertDialog? = null
    private var pendingDialog: PendingDialog? = null

    init {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                flushPendingDialog()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                dismissActiveDialog()
                pendingDialog?.cancel()
                pendingDialog = null
            }
        })
    }

    fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (result == null) return false
        enqueueDialog(PendingDialog.Alert(message.orEmpty(), result))
        return true
    }

    fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (result == null) return false
        enqueueDialog(PendingDialog.Confirm(message.orEmpty(), result))
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
        enqueueDialog(
            PendingDialog.Prompt(
                message = message.orEmpty(),
                defaultValue = defaultValue.orEmpty(),
                result = result,
            ),
        )
        return true
    }

    fun onJsBeforeUnload(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?,
    ): Boolean {
        if (result == null) return false
        enqueueDialog(
            PendingDialog.BeforeUnload(
                message = message?.takeIf { it.isNotBlank() } ?: "Leave this page?",
                result = result,
            ),
        )
        return true
    }

    private fun enqueueDialog(dialog: PendingDialog) {
        activity.runOnUiThread {
            if (activity.isFinishing || activity.isDestroyed) {
                dialog.cancel()
                return@runOnUiThread
            }
            if (!canShowDialogNow()) {
                pendingDialog?.cancel()
                pendingDialog = dialog
                return@runOnUiThread
            }
            showDialog(dialog)
        }
    }

    private fun flushPendingDialog() {
        activity.runOnUiThread {
            val dialog = pendingDialog ?: return@runOnUiThread
            if (!canShowDialogNow()) return@runOnUiThread
            pendingDialog = null
            showDialog(dialog)
        }
    }

    private fun canShowDialogNow(): Boolean {
        return activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
            !activity.isFinishing &&
            !activity.isDestroyed
    }

    private fun showDialog(dialog: PendingDialog) {
        dismissActiveDialog()
        activity.window.decorView.post {
            if (activity.isFinishing || activity.isDestroyed) {
                dialog.cancel()
                return@post
            }
            activeDialog = buildDialog(dialog).also { it.show() }
        }
    }

    private fun buildDialog(dialog: PendingDialog): AlertDialog {
        val themedContext = ContextThemeWrapper(
            activity,
            android.R.style.Theme_Material_Light_Dialog_Alert,
        )
        val builder = AlertDialog.Builder(themedContext)
            .setOnCancelListener { dialog.cancel() }
            .setOnDismissListener { activeDialog = null }

        when (dialog) {
            is PendingDialog.Alert -> {
                builder
                    .setMessage(dialog.message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> dialog.confirm() }
            }
            is PendingDialog.Confirm -> {
                builder
                    .setMessage(dialog.message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> dialog.confirm() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> dialog.cancel() }
            }
            is PendingDialog.Prompt -> {
                val input = EditText(themedContext).apply {
                    setText(dialog.defaultValue)
                }
                builder
                    .setMessage(dialog.message)
                    .setView(input)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        dialog.confirm(input.text.toString())
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> dialog.cancel() }
            }
            is PendingDialog.BeforeUnload -> {
                builder
                    .setMessage(dialog.message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> dialog.confirm() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> dialog.cancel() }
            }
        }

        return builder.create().apply {
            setCanceledOnTouchOutside(false)
            setOnCancelListener { dialog.cancel() }
        }
    }

    private fun dismissActiveDialog() {
        activeDialog?.setOnDismissListener(null)
        activeDialog?.setOnCancelListener(null)
        activeDialog?.dismiss()
        activeDialog = null
    }

    private sealed class PendingDialog {
        private val handled = AtomicBoolean(false)

        fun confirm(value: String? = null) {
            if (!handled.compareAndSet(false, true)) return
            doConfirm(value)
        }

        fun cancel() {
            if (!handled.compareAndSet(false, true)) return
            doCancel()
        }

        protected abstract fun doConfirm(value: String?)
        protected abstract fun doCancel()

        class Alert(
            val message: String,
            private val result: JsResult,
        ) : PendingDialog() {
            override fun doConfirm(value: String?) = result.confirm()
            override fun doCancel() = result.cancel()
        }

        class Confirm(
            val message: String,
            private val result: JsResult,
        ) : PendingDialog() {
            override fun doConfirm(value: String?) = result.confirm()
            override fun doCancel() = result.cancel()
        }

        class Prompt(
            val message: String,
            val defaultValue: String,
            private val result: JsPromptResult,
        ) : PendingDialog() {
            override fun doConfirm(value: String?) = result.confirm(value.orEmpty())
            override fun doCancel() = result.cancel()
        }

        class BeforeUnload(
            val message: String,
            private val result: JsResult,
        ) : PendingDialog() {
            override fun doConfirm(value: String?) = result.confirm()
            override fun doCancel() = result.cancel()
        }
    }
}

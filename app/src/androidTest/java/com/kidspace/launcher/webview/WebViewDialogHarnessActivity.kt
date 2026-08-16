package com.kidspace.launcher.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.test.core.app.ActivityScenario
import org.mockito.Mockito.mock

/**
 * Minimal harness mirroring [com.kidspace.launcher.WebViewActivity] resume ordering for dialog tests.
 */
class WebViewDialogHarnessActivity : ComponentActivity() {
    lateinit var hostResumeGate: WebViewHostResumeGate
        private set
    lateinit var jsDialogHandler: WebViewJsDialogHandler
        private set
    lateinit var webView: WebView
        private set

    var lastJsResult: JsResult? = null
        private set

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostResumeGate = WebViewHostResumeGate { !isFinishing && !isDestroyed }
        jsDialogHandler = WebViewJsDialogHandler(this, hostResumeGate)
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webChromeClient = object : WebChromeClient() {
                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: JsResult?,
                ): Boolean = jsDialogHandler.onJsConfirm(view, url, message, result)
            }
        }
        setContentView(webView)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        hostResumeGate.markReady()
        jsDialogHandler.onHostReady()
    }

    override fun onPause() {
        hostResumeGate.markNotReady()
        webView.onPause()
        super.onPause()
    }

    fun showConfirm(message: String) {
        val result = mock(JsResult::class.java)
        lastJsResult = result
        jsDialogHandler.onJsConfirm(webView, "file:///test", message, result)
    }

    fun simulateFilePickerCallbackThenConfirm(message: String) {
        hostResumeGate.markNotReady()
        hostResumeGate.runWhenReady {
            showConfirm(message)
        }
        hostResumeGate.markReady()
        jsDialogHandler.onHostReady()
    }

    companion object {
        fun launch(): ActivityScenario<WebViewDialogHarnessActivity> {
            return ActivityScenario.launch(WebViewDialogHarnessActivity::class.java)
        }
    }
}

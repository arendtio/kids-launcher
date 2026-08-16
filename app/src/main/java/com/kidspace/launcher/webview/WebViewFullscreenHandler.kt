package com.kidspace.launcher.webview

import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kidspace.launcher.data.model.PermissionPolicy

/**
 * Handles [WebChromeClient.onShowCustomView] / [WebChromeClient.onHideCustomView]
 * for HTML5 fullscreen video.
 */
class WebViewFullscreenHandler(
    private val activity: ComponentActivity,
    private val root: FrameLayout,
    private val webView: WebView,
    private val fullscreenPolicy: PermissionPolicy,
) {
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    fun onShowCustomView(view: View?, callback: WebChromeClient.CustomViewCallback?) {
        if (fullscreenPolicy != PermissionPolicy.GRANT || view == null) {
            callback?.onCustomViewHidden()
            return
        }
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view
        customViewCallback = callback
        webView.visibility = View.GONE
        root.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        enterFullscreen()
    }

    fun onHideCustomView() {
        customView?.let { root.removeView(it) }
        customView = null
        webView.visibility = View.VISIBLE
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        exitFullscreen()
    }

    fun cleanup() {
        if (customView != null) {
            onHideCustomView()
        }
    }

    private fun enterFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun exitFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

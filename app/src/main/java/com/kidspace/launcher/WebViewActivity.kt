package com.kidspace.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.util.DomainMatcher
import com.kidspace.launcher.webview.WebViewPermissionHandler

class WebViewActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var permissionHandler: WebViewPermissionHandler

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val startUrl = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }
        val cameraPolicy = intent.getStringExtra(EXTRA_CAMERA_POLICY)
            ?.let(PermissionPolicy::valueOf)
            ?: PermissionPolicy.GRANT
        val microphonePolicy = intent.getStringExtra(EXTRA_MICROPHONE_POLICY)
            ?.let(PermissionPolicy::valueOf)
            ?: PermissionPolicy.GRANT
        val locationPolicy = intent.getStringExtra(EXTRA_LOCATION_POLICY)
            ?.let(PermissionPolicy::valueOf)
            ?: PermissionPolicy.GRANT
        val normalizedUrl = DomainMatcher.normalizeUrl(startUrl)

        permissionHandler = WebViewPermissionHandler(
            activity = this,
            cameraPolicy = cameraPolicy,
            microphonePolicy = microphonePolicy,
        )

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    val target = request?.url?.toString() ?: return false
                    return !DomainMatcher.isAllowedNavigation(startUrl, target)
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    request ?: return
                    permissionHandler.handlePermissionRequest(request)
                }

                override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                    request ?: return
                    permissionHandler.onPermissionRequestCanceled(request)
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?,
                ) {
                    val allow = locationPolicy == PermissionPolicy.GRANT
                    callback?.invoke(origin, allow, false)
                }
            }
        }

        setContentView(
            webView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            permissionHandler.prepareRuntimePermissions {
                webView.loadUrl(normalizedUrl)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.onResume()
        }
    }

    override fun onPause() {
        if (::webView.isInitialized) {
            webView.onPause()
        }
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::webView.isInitialized) {
            webView.saveState(outState)
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_CAMERA_POLICY = "extra_camera_policy"
        const val EXTRA_MICROPHONE_POLICY = "extra_microphone_policy"
        const val EXTRA_LOCATION_POLICY = "extra_location_policy"
    }
}

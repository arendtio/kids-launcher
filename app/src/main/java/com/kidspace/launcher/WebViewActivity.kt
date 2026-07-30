package com.kidspace.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.util.DomainMatcher

class WebViewActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val startUrl = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }
        val cameraPolicy = intent.getStringExtra(EXTRA_CAMERA_POLICY) ?: PermissionPolicy.GRANT.name
        val microphonePolicy = intent.getStringExtra(EXTRA_MICROPHONE_POLICY) ?: PermissionPolicy.GRANT.name
        val locationPolicy = intent.getStringExtra(EXTRA_LOCATION_POLICY) ?: PermissionPolicy.GRANT.name
        val normalizedUrl = DomainMatcher.normalizeUrl(startUrl)

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
                    val allowed = request.resources.filter { resource ->
                        when (resource) {
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                                cameraPolicy == PermissionPolicy.GRANT.name
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                                microphonePolicy == PermissionPolicy.GRANT.name
                            else -> true
                        }
                    }.toTypedArray()
                    if (allowed.isNotEmpty()) {
                        request.grant(allowed)
                    } else {
                        request.deny()
                    }
                }

                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?,
                ) {
                    val allow = locationPolicy == PermissionPolicy.GRANT.name
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
            webView.loadUrl(normalizedUrl)
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

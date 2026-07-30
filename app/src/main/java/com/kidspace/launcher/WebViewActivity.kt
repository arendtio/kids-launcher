package com.kidspace.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.util.DomainMatcher

class WebViewActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startUrl = intent.getStringExtra(EXTRA_URL) ?: run {
            finish()
            return
        }
        val cameraPolicy = intent.getStringExtra(EXTRA_CAMERA_POLICY) ?: PermissionPolicy.GRANT.name
        val microphonePolicy = intent.getStringExtra(EXTRA_MICROPHONE_POLICY) ?: PermissionPolicy.GRANT.name
        val locationPolicy = intent.getStringExtra(EXTRA_LOCATION_POLICY) ?: PermissionPolicy.GRANT.name

        setContent {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setSupportMultipleWindows(false)

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

                        loadUrl(DomainMatcher.normalizeUrl(startUrl))
                    }
                },
            )
        }
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_CAMERA_POLICY = "extra_camera_policy"
        const val EXTRA_MICROPHONE_POLICY = "extra_microphone_policy"
        const val EXTRA_LOCATION_POLICY = "extra_location_policy"
    }
}

package com.kidspace.launcher

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.view.WindowCompat
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.util.DomainMatcher
import com.kidspace.launcher.webview.WebViewDownloadHandler
import com.kidspace.launcher.webview.WebViewFileChooserHandler
import com.kidspace.launcher.webview.WebViewFullscreenHandler
import com.kidspace.launcher.webview.WebViewGeolocationHandler
import com.kidspace.launcher.webview.WebViewPermissionHandler

class WebViewActivity : ComponentActivity() {

    private lateinit var rootLayout: FrameLayout
    private lateinit var webView: WebView
    private lateinit var permissionHandler: WebViewPermissionHandler
    private lateinit var fileChooserHandler: WebViewFileChooserHandler
    private lateinit var geolocationHandler: WebViewGeolocationHandler
    private lateinit var downloadHandler: WebViewDownloadHandler
    private lateinit var fullscreenHandler: WebViewFullscreenHandler

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
        val fileUploadPolicy = intent.getStringExtra(EXTRA_FILE_UPLOAD_POLICY)
            ?.let(PermissionPolicy::valueOf)
            ?: PermissionPolicy.DENY
        val downloadPolicy = intent.getStringExtra(EXTRA_DOWNLOAD_POLICY)
            ?.let(PermissionPolicy::valueOf)
            ?: PermissionPolicy.DENY
        val fullscreenPolicy = intent.getStringExtra(EXTRA_FULLSCREEN_POLICY)
            ?.let(PermissionPolicy::valueOf)
            ?: PermissionPolicy.GRANT
        val cameraCapturePolicy = intent.getStringExtra(EXTRA_CAMERA_CAPTURE_POLICY)
            ?.let(PermissionPolicy::valueOf)
            ?: PermissionPolicy.DENY
        val normalizedUrl = DomainMatcher.normalizeUrl(startUrl)

        permissionHandler = WebViewPermissionHandler(
            activity = this,
            cameraPolicy = cameraPolicy,
            microphonePolicy = microphonePolicy,
        )
        fileChooserHandler = WebViewFileChooserHandler(
            activity = this,
            fileUploadPolicy = fileUploadPolicy,
            cameraCapturePolicy = cameraCapturePolicy,
        )
        geolocationHandler = WebViewGeolocationHandler(
            activity = this,
            locationPolicy = locationPolicy,
        )
        downloadHandler = WebViewDownloadHandler(
            context = this,
            downloadPolicy = downloadPolicy,
        )

        rootLayout = FrameLayout(this)
        webView = WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
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

            setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                downloadHandler.onDownloadStart(
                    url,
                    userAgent,
                    contentDisposition,
                    mimeType,
                    contentLength,
                )
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
                    callback: android.webkit.GeolocationPermissions.Callback?,
                ) {
                    geolocationHandler.onGeolocationPermissionsShowPrompt(origin, callback)
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?,
                ): Boolean {
                    return fileChooserHandler.showChooser(filePathCallback, fileChooserParams)
                }

                override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                    fullscreenHandler.onShowCustomView(view, callback)
                }

                override fun onHideCustomView() {
                    fullscreenHandler.onHideCustomView()
                }
            }
        }

        rootLayout.addView(webView)
        fullscreenHandler = WebViewFullscreenHandler(
            activity = this,
            root = rootLayout,
            webView = webView,
            fullscreenPolicy = fullscreenPolicy,
        )

        setContentView(
            rootLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )

        onBackPressedDispatcher.addCallback(this) {
            if (webView.visibility != android.view.View.VISIBLE) {
                fullscreenHandler.onHideCustomView()
            } else if (webView.canGoBack()) {
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
        if (::fileChooserHandler.isInitialized) {
            fileChooserHandler.cancel()
        }
        if (::geolocationHandler.isInitialized) {
            geolocationHandler.cancel()
        }
        if (::fullscreenHandler.isInitialized) {
            fullscreenHandler.cleanup()
        }
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
        const val EXTRA_FILE_UPLOAD_POLICY = "extra_file_upload_policy"
        const val EXTRA_DOWNLOAD_POLICY = "extra_download_policy"
        const val EXTRA_FULLSCREEN_POLICY = "extra_fullscreen_policy"
        const val EXTRA_CAMERA_CAPTURE_POLICY = "extra_camera_capture_policy"
    }
}

package com.kidspace.launcher

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.kidspace.launcher.data.model.PermissionPolicy
import com.kidspace.launcher.util.DomainMatcher
import com.kidspace.launcher.webview.WebViewDownloadHandler
import com.kidspace.launcher.webview.WebViewFileChooserHandler
import com.kidspace.launcher.webview.WebViewFullscreenHandler
import com.kidspace.launcher.webview.WebViewGeolocationHandler
import com.kidspace.launcher.webview.WebViewHostResumeGate
import com.kidspace.launcher.webview.WebViewHostViewModel
import com.kidspace.launcher.webview.WebViewJsDialogHandler
import com.kidspace.launcher.webview.WebViewPermissionHandler
import com.kidspace.launcher.webview.WebViewFileChooserCallbackStore
import com.kidspace.launcher.webview.WebViewUploadSession
import com.kidspace.launcher.webview.WebViewImportSession
import com.kidspace.launcher.webview.WebViewUploadDebugTrace

class WebViewActivity : ComponentActivity() {

    private val viewModel: WebViewHostViewModel by viewModels()

    private lateinit var rootLayout: FrameLayout
    private lateinit var webView: WebView
    private lateinit var permissionHandler: WebViewPermissionHandler
    private lateinit var fileChooserHandler: WebViewFileChooserHandler
    private lateinit var geolocationHandler: WebViewGeolocationHandler
    private lateinit var downloadHandler: WebViewDownloadHandler
    private lateinit var fullscreenHandler: WebViewFullscreenHandler
    private lateinit var jsDialogHandler: WebViewJsDialogHandler
    private lateinit var hostResumeGate: WebViewHostResumeGate
    private var uploadDebugTrace: WebViewUploadDebugTrace? = null
    private lateinit var startUrl: String
    private var restoredFromUploadSession = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        startUrl = intent.getStringExtra(EXTRA_URL) ?: run {
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
        val uploadDebugEnabled = fileUploadPolicy == PermissionPolicy.GRANT
        var uploadDebugText: TextView? = null
        uploadDebugTrace = WebViewUploadDebugTrace(uploadDebugEnabled) { text ->
            runOnUiThread { uploadDebugText?.text = text }
        }

        hostResumeGate = WebViewHostResumeGate { !isFinishing && !isDestroyed }
        rootLayout = FrameLayout(this)
        webView = obtainWebView()

        permissionHandler = WebViewPermissionHandler(
            activity = this,
            cameraPolicy = cameraPolicy,
            microphonePolicy = microphonePolicy,
        )
        fileChooserHandler = WebViewFileChooserHandler(
            activity = this,
            hostResumeGate = hostResumeGate,
            fileUploadPolicy = fileUploadPolicy,
            cameraCapturePolicy = cameraCapturePolicy,
            webViewProvider = { if (::webView.isInitialized) webView else null },
            debugTrace = uploadDebugTrace,
        )
        geolocationHandler = WebViewGeolocationHandler(
            activity = this,
            locationPolicy = locationPolicy,
        )
        downloadHandler = WebViewDownloadHandler(
            context = this,
            downloadPolicy = downloadPolicy,
        )
        jsDialogHandler = WebViewJsDialogHandler(
            activity = this,
            hostResumeGate = hostResumeGate,
            debugTrace = uploadDebugTrace,
        )
        bindWebViewClients(normalizedUrl)

        rootLayout.addView(webView)
        if (uploadDebugEnabled) {
            uploadDebugText = attachUploadDebugOverlay()
            uploadDebugTrace?.event("Upload-Debug aktiv für $normalizedUrl")
        }
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

        when {
            restoredFromUploadSession || WebViewFileChooserCallbackStore.pickerInFlight -> {
                uploadDebugTrace?.event("skipped restoreState (upload session active, url=${webView.url})")
            }
            savedInstanceState != null -> {
                webView.restoreState(savedInstanceState)
            }
            webView.url.isNullOrBlank() -> {
                webView.loadUrl(normalizedUrl)
            }
            else -> {
                uploadDebugTrace?.event("WebView kept loaded at ${webView.url}")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun obtainWebView(): WebView {
        WebViewUploadSession.activeWebView()?.let { retained ->
            viewModel.webView = retained
            restoredFromUploadSession = true
            uploadDebugTrace?.event("WebView restored from upload session id=${retained.hashCode()}")
            return retained
        }
        restoredFromUploadSession = false
        viewModel.webView?.let { existing ->
            (existing.parent as? ViewGroup)?.removeView(existing)
            uploadDebugTrace?.event("WebView reused from ViewModel id=${existing.hashCode()}")
            return existing
        }
        return WebView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowContentAccess = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mediaPlaybackRequiresUserGesture = false
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            viewModel.webView = this
        }
    }

    private fun bindWebViewClients(normalizedUrl: String) {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val target = request?.url?.toString() ?: return false
                return !DomainMatcher.isAllowedNavigation(startUrl, target)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                uploadDebugTrace?.event("page started url=$url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                uploadDebugTrace?.event("page finished url=$url")
                if (WebViewImportSession.importInFlight) {
                    WebViewImportSession.markImportFinished()
                    uploadDebugTrace?.event("import guard released after reload")
                }
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            downloadHandler.onDownloadStart(
                url,
                userAgent,
                contentDisposition,
                mimeType,
                contentLength,
            )
        }

        webView.webChromeClient = object : WebChromeClient() {
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
            ): Boolean = fileChooserHandler.showChooser(webView, filePathCallback, fileChooserParams)

            override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                fullscreenHandler.onShowCustomView(view, callback)
            }

            override fun onHideCustomView() {
                fullscreenHandler.onHideCustomView()
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?,
            ): Boolean = jsDialogHandler.onJsAlert(view, url, message, result)

            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?,
            ): Boolean = jsDialogHandler.onJsConfirm(view, url, message, result)

            override fun onJsPrompt(
                view: WebView?,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: android.webkit.JsPromptResult?,
            ): Boolean = jsDialogHandler.onJsPrompt(view, url, message, defaultValue, result)

            override fun onJsBeforeUnload(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?,
            ): Boolean = jsDialogHandler.onJsBeforeUnload(view, url, message, result)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::webView.isInitialized) {
            webView.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.onResume()
        }
        if (::hostResumeGate.isInitialized) {
            hostResumeGate.markReady()
            jsDialogHandler.onHostReady()
        }
    }

    override fun onPause() {
        if (::hostResumeGate.isInitialized) {
            hostResumeGate.markNotReady()
        }
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
            if (!WebViewImportSession.importInFlight) {
                webView.stopLoading()
            } else {
                uploadDebugTrace?.event("stopLoading skipped (import in flight)")
            }
            if (WebViewImportSession.shouldRetainWebView() ||
                WebViewFileChooserCallbackStore.pickerInFlight
            ) {
                WebViewUploadSession.retain(webView)
            } else {
                (webView.parent as? ViewGroup)?.removeView(webView)
            }
        }
        super.onDestroy()
    }

    private fun attachUploadDebugOverlay(): TextView {
        val textView = TextView(this).apply {
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xCC000000.toInt())
            textSize = 11f
            setPadding(16, 12, 16, 12)
            text = "Upload-Debug aktiv. Wähle eine Datei …"
        }
        val scrollView = ScrollView(this).apply {
            addView(textView)
        }
        val shareButton = Button(this).apply {
            text = "Log teilen"
            setOnClickListener { shareUploadDebugLog() }
        }
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xCC000000.toInt())
            addView(
                scrollView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f,
                ),
            )
            addView(shareButton)
        }
        rootLayout.addView(
            panel,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (resources.displayMetrics.heightPixels * 0.28f).toInt(),
                Gravity.BOTTOM,
            ),
        )
        return textView
    }

    private fun shareUploadDebugLog() {
        val text = uploadDebugTrace?.shareText() ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "KidSpace Upload Debug")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Upload-Debug teilen"))
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

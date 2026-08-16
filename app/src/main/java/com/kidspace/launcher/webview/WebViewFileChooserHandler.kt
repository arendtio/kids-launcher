package com.kidspace.launcher.webview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.kidspace.launcher.data.model.PermissionPolicy
import java.io.File

/**
 * Handles [WebChromeClient.onShowFileChooser] for `<input type="file">` in the in-app browser.
 */
class WebViewFileChooserHandler(
    private val activity: ComponentActivity,
    private val fileUploadPolicy: PermissionPolicy,
    private val cameraCapturePolicy: PermissionPolicy,
    private val webViewProvider: () -> WebView?,
    private val debugTrace: WebViewUploadDebugTrace? = null,
) {
    private var pendingCallback: ValueCallback<Array<Uri>>? = null
    private var boundWebView: WebView? = null
    private var pendingCaptureAfterPermission = false
    private var photoUri: Uri? = null

    private val fileChooserLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        result.data?.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        debugTrace?.event(
            "picker result code=${result.resultCode} data=${result.data != null}",
        )
        val uris = WebChromeClient.FileChooserParams.parseResult(
            result.resultCode,
            result.data,
        )
        debugTrace?.event(
            "parseResult uris=${uris?.size ?: 0} first=${uris?.firstOrNull()?.toString()?.take(80)}",
        )
        deliverResult(uris)
    }

    private val takePictureLauncher = activity.registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = photoUri
        photoUri = null
        deliverResult(if (success && uri != null) arrayOf(uri) else null)
    }

    private val cameraPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && pendingCaptureAfterPermission) {
            pendingCaptureAfterPermission = false
            launchTakePicture()
        } else {
            pendingCaptureAfterPermission = false
            deliverResult(null)
        }
    }

    fun showChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        params: WebChromeClient.FileChooserParams?,
    ): Boolean {
        if (filePathCallback == null) return false

        boundWebView = webView ?: webViewProvider()
        boundWebView?.let { WebViewUploadSession.bind(it) }

        debugTrace?.event(
            "showChooser webView=${boundWebView?.hashCode()} accept=${params?.acceptTypes?.joinToString()} " +
                "mode=${params?.mode} policy=$fileUploadPolicy",
        )

        resolvePreviousCallback()?.onReceiveValue(null)
        pendingCallback = filePathCallback
        WebViewFileChooserCallbackStore.setPending(filePathCallback)

        if (WebViewFileChooserLogic.shouldUseCameraCapture(params)) {
            if (!WebViewFileChooserLogic.isCaptureAllowed(cameraCapturePolicy)) {
                debugTrace?.event("camera capture denied by policy")
                deliverResult(null)
                return true
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                pendingCaptureAfterPermission = true
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                return true
            }
            launchTakePicture()
            return true
        }

        if (!WebViewFileChooserLogic.isFileUploadAllowed(fileUploadPolicy)) {
            debugTrace?.event("file upload denied by policy")
            deliverResult(null)
            return true
        }

        launchDocumentPicker(params)
        return true
    }

    fun cancel(force: Boolean = false) {
        if (!force && WebViewFileChooserCallbackStore.pickerInFlight) {
            debugTrace?.event("cancel skipped (picker in flight)")
            pendingCallback = null
            return
        }
        if (!force && WebViewImportSession.importInFlight) {
            debugTrace?.event("cancel skipped (import in flight)")
            pendingCallback = null
            return
        }
        debugTrace?.event("file chooser cancelled")
        pendingCaptureAfterPermission = false
        photoUri = null
        deliverResult(null)
    }

    private fun launchDocumentPicker(params: WebChromeClient.FileChooserParams?) {
        val intent = params?.createIntent() ?: buildFallbackIntent(params)
        debugTrace?.event("launch picker action=${intent.action} type=${intent.type}")
        try {
            fileChooserLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            debugTrace?.event("picker ActivityNotFoundException")
            deliverResult(null)
        }
    }

    private fun buildFallbackIntent(params: WebChromeClient.FileChooserParams?): Intent {
        val mimeTypes = WebViewFileChooserLogic.mimeTypesFromAcceptTypes(params?.acceptTypes)
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            if (params?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
    }

    private fun launchTakePicture() {
        val file = File(activity.cacheDir, "upload_capture_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file,
        )
        takePictureLauncher.launch(requireNotNull(photoUri))
    }

    private fun deliverResult(uris: Array<Uri>?) {
        val callback = resolvePendingCallback()
        if (callback == null) {
            debugTrace?.event("deliverResult skipped (no callback)")
            return
        }

        if (uris == null) {
            debugTrace?.event("deliverResult null → onReceiveValue(null)")
            postToTargetWebView { callback.onReceiveValue(null) }
            return
        }

        val prepared = WebViewFileChooserUriAccess.prepareForWebView(activity, uris)
        prepared.forEach { uri -> grantReadPermission(uri) }
        debugTrace?.event(
            "deliverResult uris=" +
                prepared.joinToString { describeUri(activity, it) },
        )
        val targetWebView = resolveTargetWebView()
        debugTrace?.event("deliver to webView id=${targetWebView?.hashCode()}")
        postToTargetWebView(targetWebView) {
            callback.onReceiveValue(prepared)
            WebViewFileChooserCallbackStore.markRecentFileDelivery()
            debugTrace?.event("onReceiveValue called")
            boundWebView = null
        }
    }

    private fun resolveTargetWebView(): WebView? {
        return boundWebView ?: webViewProvider() ?: WebViewUploadSession.activeWebView()
    }

    private fun postToTargetWebView(webView: WebView? = resolveTargetWebView(), action: () -> Unit) {
        if (webView != null) {
            webView.post { action() }
        } else {
            action()
        }
    }

    private fun resolvePendingCallback(): ValueCallback<Array<Uri>>? {
        pendingCallback?.let {
            pendingCallback = null
            WebViewFileChooserCallbackStore.clear()
            return it
        }
        val restored = WebViewFileChooserCallbackStore.take()
        if (restored != null) {
            debugTrace?.event("restored callback from store after activity recreation")
        }
        return restored
    }

    private fun resolvePreviousCallback(): ValueCallback<Array<Uri>>? {
        pendingCallback?.let { return it }
        return WebViewFileChooserCallbackStore.peek()
    }

    private fun describeUri(context: ComponentActivity, uri: Uri): String {
        val size = runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: -1L
        }.getOrDefault(-1L)
        return "${uri.authority} size=$size"
    }

    private fun grantReadPermission(uri: Uri) {
        activity.grantUriPermission(
            activity.packageName,
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        runCatching {
            activity.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }
}

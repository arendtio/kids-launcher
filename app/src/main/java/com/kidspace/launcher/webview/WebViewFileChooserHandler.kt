package com.kidspace.launcher.webview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.kidspace.launcher.data.model.PermissionPolicy
import java.io.File

/**
 * Handles [WebChromeClient.onShowFileChooser] for `<input type="file">` in the in-app browser.
 *
 * URIs must be delivered to [ValueCallback.onReceiveValue] immediately when the picker returns.
 * Deferring breaks `input.files` population in many WebViews (the page never reaches `confirm()`).
 */
class WebViewFileChooserHandler(
    private val activity: ComponentActivity,
    private val fileUploadPolicy: PermissionPolicy,
    private val cameraCapturePolicy: PermissionPolicy,
) {
    private var pendingCallback: ValueCallback<Array<Uri>>? = null
    private var pendingCaptureAfterPermission = false
    private var photoUri: Uri? = null

    private val fileChooserLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(
            result.resultCode,
            result.data,
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
        filePathCallback: ValueCallback<Array<Uri>>?,
        params: WebChromeClient.FileChooserParams?,
    ): Boolean {
        if (filePathCallback == null) return false

        pendingCallback?.onReceiveValue(null)
        pendingCallback = filePathCallback

        if (WebViewFileChooserLogic.shouldUseCameraCapture(params)) {
            if (!WebViewFileChooserLogic.isCaptureAllowed(cameraCapturePolicy)) {
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
            deliverResult(null)
            return true
        }

        launchDocumentPicker(params)
        return true
    }

    fun cancel() {
        pendingCaptureAfterPermission = false
        photoUri = null
        deliverResult(null)
    }

    private fun launchDocumentPicker(params: WebChromeClient.FileChooserParams?) {
        val intent = params?.createIntent() ?: buildFallbackIntent(params)
        try {
            fileChooserLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
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
        val callback = pendingCallback
        pendingCallback = null
        if (callback == null) return

        if (uris == null) {
            callback.onReceiveValue(null)
            return
        }

        val prepared = WebViewFileChooserUriAccess.prepareForWebView(activity, uris)
        prepared.forEach { uri -> grantReadPermission(uri) }
        callback.onReceiveValue(prepared)
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

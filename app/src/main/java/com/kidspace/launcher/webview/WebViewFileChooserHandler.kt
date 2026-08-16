package com.kidspace.launcher.webview

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
 * Regular file picking is gated by [fileUploadPolicy]. Camera capture (`capture` attribute) uses
 * [cameraCapturePolicy] separately from WebRTC camera access.
 */
class WebViewFileChooserHandler(
    private val activity: ComponentActivity,
    private val fileUploadPolicy: PermissionPolicy,
    private val cameraCapturePolicy: PermissionPolicy,
) {
    private var pendingCallback: ValueCallback<Array<Uri>>? = null
    private var pendingCaptureAfterPermission = false
    private var photoUri: Uri? = null

    private val singleFileLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        deliverResult(if (uri != null) arrayOf(uri) else null)
    }

    private val multipleFileLauncher = activity.registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris ->
        deliverResult(if (uris.isEmpty()) null else uris.toTypedArray())
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

        val mimeTypes = WebViewFileChooserLogic.mimeTypesFromAcceptTypes(params?.acceptTypes)
        val mode = params?.mode ?: WebChromeClient.FileChooserParams.MODE_OPEN
        if (mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
            multipleFileLauncher.launch(mimeTypes)
        } else {
            singleFileLauncher.launch(mimeTypes)
        }
        return true
    }

    fun cancel() {
        pendingCaptureAfterPermission = false
        photoUri = null
        deliverResult(null)
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
        uris?.forEach { uri ->
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
        val callback = pendingCallback
        pendingCallback = null
        callback?.onReceiveValue(uris)
    }
}

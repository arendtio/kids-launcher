package com.kidspace.launcher.webview

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.kidspace.launcher.data.model.PermissionPolicy

/**
 * Handles [WebChromeClient.onShowFileChooser] for `<input type="file">` in the in-app browser.
 *
 * File uploads are gated by the parent-configured [fileUploadPolicy] for the link.
 */
class WebViewFileChooserHandler(
    private val activity: ComponentActivity,
    private val fileUploadPolicy: PermissionPolicy,
) {
    private var pendingCallback: ValueCallback<Array<Uri>>? = null

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

    fun showChooser(
        filePathCallback: ValueCallback<Array<Uri>>?,
        params: WebChromeClient.FileChooserParams?,
    ): Boolean {
        if (filePathCallback == null) return false

        if (!WebViewFileChooserLogic.isFileUploadAllowed(fileUploadPolicy)) {
            filePathCallback.onReceiveValue(null)
            return true
        }

        pendingCallback?.onReceiveValue(null)
        pendingCallback = filePathCallback

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
        pendingCallback?.onReceiveValue(null)
        pendingCallback = null
    }

    private fun deliverResult(uris: Array<Uri>?) {
        val callback = pendingCallback
        pendingCallback = null
        callback?.onReceiveValue(uris)
    }
}

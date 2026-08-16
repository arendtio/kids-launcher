package com.kidspace.launcher.webview

import android.webkit.WebChromeClient
import com.kidspace.launcher.data.model.PermissionPolicy

object WebViewFileChooserLogic {
    fun isFileUploadAllowed(fileUploadPolicy: PermissionPolicy): Boolean =
        fileUploadPolicy == PermissionPolicy.GRANT

    fun isCaptureAllowed(cameraCapturePolicy: PermissionPolicy): Boolean =
        cameraCapturePolicy == PermissionPolicy.GRANT

    fun shouldUseCameraCapture(params: WebChromeClient.FileChooserParams?): Boolean =
        params?.isCaptureEnabled == true

    fun mimeTypesFromAcceptTypes(acceptTypes: Array<String>?): Array<String> {
        val filtered = acceptTypes?.filter { it.isNotBlank() } ?: emptyList()
        return if (filtered.isEmpty()) arrayOf("*/*") else filtered.toTypedArray()
    }
}

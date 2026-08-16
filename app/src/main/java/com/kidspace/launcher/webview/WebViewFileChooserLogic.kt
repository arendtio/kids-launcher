package com.kidspace.launcher.webview

import com.kidspace.launcher.data.model.PermissionPolicy

object WebViewFileChooserLogic {
    fun isFileUploadAllowed(fileUploadPolicy: PermissionPolicy): Boolean =
        fileUploadPolicy == PermissionPolicy.GRANT

    fun mimeTypesFromAcceptTypes(acceptTypes: Array<String>?): Array<String> {
        val filtered = acceptTypes?.filter { it.isNotBlank() } ?: emptyList()
        return if (filtered.isEmpty()) arrayOf("*/*") else filtered.toTypedArray()
    }
}

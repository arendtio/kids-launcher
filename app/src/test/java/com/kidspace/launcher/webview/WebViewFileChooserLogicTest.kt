package com.kidspace.launcher.webview

import com.kidspace.launcher.data.model.PermissionPolicy
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewFileChooserLogicTest {
    @Test
    fun isFileUploadAllowed_grantReturnsTrue() {
        assertTrue(WebViewFileChooserLogic.isFileUploadAllowed(PermissionPolicy.GRANT))
    }

    @Test
    fun isFileUploadAllowed_denyReturnsFalse() {
        assertFalse(WebViewFileChooserLogic.isFileUploadAllowed(PermissionPolicy.DENY))
    }

    @Test
    fun isCaptureAllowed_grantReturnsTrue() {
        assertTrue(WebViewFileChooserLogic.isCaptureAllowed(PermissionPolicy.GRANT))
    }

    @Test
    fun isCaptureAllowed_denyReturnsFalse() {
        assertFalse(WebViewFileChooserLogic.isCaptureAllowed(PermissionPolicy.DENY))
    }

    @Test
    fun shouldUseCameraCapture_nullParamsReturnsFalse() {
        assertFalse(WebViewFileChooserLogic.shouldUseCameraCapture(null))
    }

    @Test
    fun mimeTypesFromAcceptTypes_emptyUsesWildcard() {
        assertArrayEquals(arrayOf("*/*"), WebViewFileChooserLogic.mimeTypesFromAcceptTypes(null))
        assertArrayEquals(arrayOf("*/*"), WebViewFileChooserLogic.mimeTypesFromAcceptTypes(emptyArray()))
    }

    @Test
    fun mimeTypesFromAcceptTypes_mapsZipBackupAcceptList() {
        val normalized = WebViewFileChooserLogic.mimeTypesFromAcceptTypes(
            arrayOf(".zip", "application/zip"),
        )
        assertTrue(normalized.contains("application/zip"))
        assertTrue(normalized.contains("application/octet-stream"))
    }
}

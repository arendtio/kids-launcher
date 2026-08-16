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
    fun mimeTypesFromAcceptTypes_emptyUsesWildcard() {
        assertArrayEquals(arrayOf("*/*"), WebViewFileChooserLogic.mimeTypesFromAcceptTypes(null))
        assertArrayEquals(arrayOf("*/*"), WebViewFileChooserLogic.mimeTypesFromAcceptTypes(emptyArray()))
    }

    @Test
    fun mimeTypesFromAcceptTypes_filtersBlankEntries() {
        assertArrayEquals(
            arrayOf("image/*", "application/pdf"),
            WebViewFileChooserLogic.mimeTypesFromAcceptTypes(arrayOf("image/*", "", "application/pdf")),
        )
    }
}

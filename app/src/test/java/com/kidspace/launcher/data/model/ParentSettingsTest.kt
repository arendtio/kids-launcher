package com.kidspace.launcher.data.model

import org.junit.Assert.assertFalse
import org.junit.Test

class ParentSettingsTest {

    @Test
    fun uploadDebugDisabledByDefault() {
        assertFalse(ParentSettings().webViewUploadDebugEnabled)
    }
}

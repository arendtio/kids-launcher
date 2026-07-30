package com.kidspace.launcher.update

import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateConfigTest {

    @Test
    fun `debug apk url points to github raw release`() {
        assertTrue(AppUpdateConfig.DEBUG_APK_URL.startsWith("https://raw.githubusercontent.com/"))
        assertTrue(AppUpdateConfig.DEBUG_APK_URL.endsWith("/releases/KidSpace-debug.apk"))
    }
}

package com.kidspace.launcher.shortcuts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutTargetTest {
    @Test
    fun encodeAndDecode_pinnedShortcut() {
        val encoded = ShortcutTarget.encode("com.android.chrome", "shortcut-123")
        assertEquals("shortcut:com.android.chrome#shortcut-123", encoded)
        val decoded = ShortcutTarget.decode(encoded) as ShortcutTarget.ShortcutRef.Pinned
        assertEquals("com.android.chrome", decoded.hostPackage)
        assertEquals("shortcut-123", decoded.shortcutId)
    }

    @Test
    fun encodeAndDecode_legacyShortcut() {
        val encoded = ShortcutTarget.encodeLegacy("abc-uuid")
        assertTrue(ShortcutTarget.isShortcutTarget(encoded))
        val decoded = ShortcutTarget.decode(encoded) as ShortcutTarget.ShortcutRef.Legacy
        assertEquals("abc-uuid", decoded.legacyId)
    }

    @Test
    fun decode_returnsNullForAppPackage() {
        assertNull(ShortcutTarget.decode("com.android.chrome"))
    }

    @Test
    fun decode_handlesShortcutIdContainingHash() {
        val encoded = ShortcutTarget.encode("com.android.chrome", "id#with#hash")
        val decoded = ShortcutTarget.decode(encoded) as ShortcutTarget.ShortcutRef.Pinned
        assertEquals("id#with#hash", decoded.shortcutId)
    }

    @Test
    fun isShortcutTarget_detectsShortcutTargets() {
        assertTrue(ShortcutTarget.isShortcutTarget("shortcut:com.android.chrome#abc"))
        assertFalse(ShortcutTarget.isShortcutTarget("com.android.chrome"))
    }

    @Test
    fun decode_invalidFormatsReturnNull() {
        assertNull(ShortcutTarget.decode("shortcut:"))
        assertNull(ShortcutTarget.decode("shortcut:hostonly"))
        assertNotNull(ShortcutTarget.decode("shortcut:host#id"))
    }
}

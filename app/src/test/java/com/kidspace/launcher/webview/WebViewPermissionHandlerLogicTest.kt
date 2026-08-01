package com.kidspace.launcher.webview

import android.Manifest
import android.webkit.PermissionRequest
import com.kidspace.launcher.data.model.PermissionPolicy
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewPermissionHandlerLogicTest {

    @Test
    fun `audio capture requires record audio and modify audio settings in manifest`() {
        val permissions = WebViewPermissionLogic.manifestPermissionsFor(
            PermissionRequest.RESOURCE_AUDIO_CAPTURE,
        )
        assertEquals(
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
            ),
            permissions,
        )
    }

    @Test
    fun `runtime permissions include microphone for audio capture resource`() {
        val permissions = WebViewPermissionLogic.runtimePermissionsFor(
            arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE),
        )
        assertEquals(listOf(Manifest.permission.RECORD_AUDIO), permissions)
    }

    @Test
    fun `runtime permissions include camera and microphone for both resources`() {
        val permissions = WebViewPermissionLogic.runtimePermissionsFor(
            arrayOf(
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
            ),
        )
        assertEquals(
            listOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
            permissions,
        )
    }

    @Test
    fun `policy filters denied microphone resource`() {
        val allowed = WebViewPermissionLogic.resourcesAllowedByPolicy(
            arrayOf(
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
            ),
            cameraPolicy = PermissionPolicy.GRANT,
            microphonePolicy = PermissionPolicy.DENY,
        )
        assertArrayEquals(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE), allowed)
    }

    @Test
    fun `unsupported resources are rejected`() {
        val supported = WebViewPermissionLogic.supportedResourcesFrom(
            arrayOf(
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_MIDI_SYSEX,
            ),
        )
        assertTrue(supported.isEmpty())
    }

    @Test
    fun `partial grant returns audio when only microphone runtime permission granted`() {
        val grantable = WebViewPermissionLogic.grantableResourcesAfterRuntimeResult(
            requested = arrayOf(
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
            ),
            cameraPolicy = PermissionPolicy.GRANT,
            microphonePolicy = PermissionPolicy.GRANT,
            isGranted = { permission ->
                permission == Manifest.permission.RECORD_AUDIO ||
                    permission == Manifest.permission.MODIFY_AUDIO_SETTINGS
            },
        )
        assertArrayEquals(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE), grantable)
    }
}

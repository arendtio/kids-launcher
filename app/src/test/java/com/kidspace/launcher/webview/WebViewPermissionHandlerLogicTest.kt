package com.kidspace.launcher.webview

import android.Manifest
import android.webkit.PermissionRequest
import com.kidspace.launcher.data.model.PermissionPolicy
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class WebViewPermissionHandlerLogicTest {

    @Test
    fun `android permissions include microphone for audio capture resource`() {
        val permissions = androidPermissionsForResources(
            arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE),
        )
        assertEquals(listOf(Manifest.permission.RECORD_AUDIO), permissions)
    }

    @Test
    fun `android permissions include camera and microphone for both resources`() {
        val permissions = androidPermissionsForResources(
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
        val allowed = resourcesAllowedByPolicy(
            arrayOf(
                PermissionRequest.RESOURCE_AUDIO_CAPTURE,
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
            ),
            cameraPolicy = PermissionPolicy.GRANT,
            microphonePolicy = PermissionPolicy.DENY,
        )
        assertArrayEquals(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE), allowed)
    }

    private fun androidPermissionsForResources(webResources: Array<String>): List<String> {
        val permissions = linkedSetOf<String>()
        if (webResources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (webResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions.toList()
    }

    private fun resourcesAllowedByPolicy(
        resources: Array<String>,
        cameraPolicy: PermissionPolicy,
        microphonePolicy: PermissionPolicy,
    ): Array<String> = resources.filter { resource ->
        when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> cameraPolicy == PermissionPolicy.GRANT
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> microphonePolicy == PermissionPolicy.GRANT
            else -> true
        }
    }.toTypedArray()
}

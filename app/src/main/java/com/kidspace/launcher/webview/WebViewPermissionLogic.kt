package com.kidspace.launcher.webview

import android.Manifest
import android.webkit.PermissionRequest
import com.kidspace.launcher.data.model.PermissionPolicy

/**
 * Pure helpers for Android WebView media permission handling.
 *
 * WebRTC in WebView requires two separate layers:
 * 1. Android runtime permissions (RECORD_AUDIO, CAMERA) via the system dialog
 * 2. WebView [PermissionRequest.grant] in [WebChromeClient.onPermissionRequest]
 *
 * See Google's sample: googlesamples/android-PermissionRequest
 * and Hotwire's WebViewPermissionDelegate for reference implementations.
 */
object WebViewPermissionLogic {
    val supportedResources = setOf(
        PermissionRequest.RESOURCE_AUDIO_CAPTURE,
        PermissionRequest.RESOURCE_VIDEO_CAPTURE,
    )

    val runtimePermissions = setOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
    )

    fun manifestPermissionsFor(resource: String): List<String> = when (resource) {
        PermissionRequest.RESOURCE_AUDIO_CAPTURE -> listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
        )
        PermissionRequest.RESOURCE_VIDEO_CAPTURE -> listOf(
            Manifest.permission.CAMERA,
        )
        else -> emptyList()
    }

    fun resourcesAllowedByPolicy(
        resources: Array<String>,
        cameraPolicy: PermissionPolicy,
        microphonePolicy: PermissionPolicy,
    ): Array<String> = resources.filter { resource ->
        when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                cameraPolicy == PermissionPolicy.GRANT
            PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                microphonePolicy == PermissionPolicy.GRANT
            else -> false
        }
    }.toTypedArray()

    fun supportedResourcesFrom(requested: Array<String>): Array<String> {
        val supported = requested.filter { it in supportedResources }
        return if (supported.size == requested.size) supported.toTypedArray() else emptyArray()
    }

    fun runtimePermissionsFor(webResources: Array<String>): List<String> {
        val permissions = linkedSetOf<String>()
        if (webResources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (webResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions.toList()
    }

    fun grantableResourcesAfterRuntimeResult(
        requested: Array<String>,
        cameraPolicy: PermissionPolicy,
        microphonePolicy: PermissionPolicy,
        isGranted: (String) -> Boolean,
    ): Array<String> {
        val allowedByPolicy = resourcesAllowedByPolicy(requested, cameraPolicy, microphonePolicy)
        return allowedByPolicy.filter { resource ->
            manifestPermissionsFor(resource).all(isGranted)
        }.toTypedArray()
    }
}

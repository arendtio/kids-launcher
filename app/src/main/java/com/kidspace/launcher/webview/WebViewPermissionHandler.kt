package com.kidspace.launcher.webview

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.kidspace.launcher.data.model.PermissionPolicy

class WebViewPermissionHandler(
    private val activity: ComponentActivity,
    private val cameraPolicy: PermissionPolicy,
    private val microphonePolicy: PermissionPolicy,
) {
    private var pendingRequest: PermissionRequest? = null

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val request = pendingRequest
        pendingRequest = null
        if (request == null) return@registerForActivityResult
        if (results.values.all { it }) {
            grantWebViewResources(request)
        } else {
            request.deny()
        }
    }

    fun handlePermissionRequest(request: PermissionRequest) {
        val webResources = resourcesAllowedByPolicy(request)
        if (webResources.isEmpty()) {
            request.deny()
            return
        }

        val androidPermissions = androidPermissionsFor(webResources)
        if (androidPermissions.isEmpty()) {
            request.grant(webResources)
            return
        }

        if (androidPermissions.all { isGranted(it) }) {
            request.grant(webResources)
            return
        }

        pendingRequest = request
        permissionLauncher.launch(androidPermissions.toTypedArray())
    }

    fun onPermissionRequestCanceled(request: PermissionRequest) {
        if (pendingRequest == request) {
            pendingRequest = null
        }
    }

    private fun resourcesAllowedByPolicy(request: PermissionRequest): Array<String> =
        request.resources.filter { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                    cameraPolicy == PermissionPolicy.GRANT
                PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                    microphonePolicy == PermissionPolicy.GRANT
                else -> true
            }
        }.toTypedArray()

    private fun grantWebViewResources(request: PermissionRequest) {
        val allowed = resourcesAllowedByPolicy(request)
        if (allowed.isEmpty()) {
            request.deny()
        } else {
            request.grant(allowed)
        }
    }

    private fun androidPermissionsFor(webResources: Array<String>): List<String> {
        val permissions = linkedSetOf<String>()
        if (webResources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (webResources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions.toList()
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            activity,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
}

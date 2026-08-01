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
    private var pendingAction: PendingAction? = null

    private sealed class PendingAction {
        data class Prepare(val onReady: () -> Unit) : PendingAction()
        data class WebView(val request: PermissionRequest) : PendingAction()
    }

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val action = pendingAction
        pendingAction = null
        activity.runOnUiThread {
            when (action) {
                is PendingAction.Prepare -> {
                    if (results.values.all { it }) {
                        action.onReady()
                    } else {
                        action.onReady()
                    }
                }
                is PendingAction.WebView -> {
                    if (results.values.all { it }) {
                        grantWebViewResources(action.request)
                    } else {
                        action.request.deny()
                    }
                }
                null -> Unit
            }
        }
    }

    fun prepareRuntimePermissions(onReady: () -> Unit) {
        val androidPermissions = proactiveAndroidPermissions()
        if (androidPermissions.isEmpty() || androidPermissions.all { isGranted(it) }) {
            onReady()
            return
        }
        pendingAction = PendingAction.Prepare(onReady)
        permissionLauncher.launch(androidPermissions.toTypedArray())
    }

    fun handlePermissionRequest(request: PermissionRequest) {
        activity.runOnUiThread {
            val webResources = resourcesAllowedByPolicy(request)
            if (webResources.isEmpty()) {
                request.deny()
                return@runOnUiThread
            }

            val androidPermissions = androidPermissionsFor(webResources)
            if (androidPermissions.isEmpty() || androidPermissions.all { isGranted(it) }) {
                request.grant(webResources)
                return@runOnUiThread
            }

            pendingAction = PendingAction.WebView(request)
            permissionLauncher.launch(androidPermissions.toTypedArray())
        }
    }

    fun onPermissionRequestCanceled(request: PermissionRequest) {
        val action = pendingAction
        if (action is PendingAction.WebView && action.request == request) {
            pendingAction = null
        }
    }

    private fun proactiveAndroidPermissions(): List<String> {
        val permissions = linkedSetOf<String>()
        if (microphonePolicy == PermissionPolicy.GRANT) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (cameraPolicy == PermissionPolicy.GRANT) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions.toList()
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

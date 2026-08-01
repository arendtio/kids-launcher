package com.kidspace.launcher.webview

import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.kidspace.launcher.data.model.PermissionPolicy

/**
 * Handles [WebChromeClient.onPermissionRequest] for camera/microphone WebRTC.
 *
 * Flow (per Android docs and googlesamples/android-PermissionRequest):
 * 1. Web page calls getUserMedia → onPermissionRequest fires
 * 2. If Android runtime permission is missing, store the [PermissionRequest] and ask the system
 * 3. After the user responds, call [PermissionRequest.grant] with the allowed WebView resources
 *
 * Never call grant() without the matching Android runtime permission — doing so can break
 * future permission requests in the same WebView session.
 */
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

        activity.runOnUiThread {
            respondToRequest(request, results)
        }
    }

    fun handlePermissionRequest(request: PermissionRequest) {
        activity.runOnUiThread {
            handlePermissionRequestOnUiThread(request)
        }
    }

    fun onPermissionRequestCanceled(request: PermissionRequest) {
        if (pendingRequest === request) {
            pendingRequest = null
        }
    }

    private fun handlePermissionRequestOnUiThread(request: PermissionRequest) {
        val requested = request.resources ?: emptyArray()
        val supported = WebViewPermissionLogic.supportedResourcesFrom(requested)
        if (supported.isEmpty()) {
            request.deny()
            return
        }

        val allowedByPolicy = WebViewPermissionLogic.resourcesAllowedByPolicy(
            supported,
            cameraPolicy,
            microphonePolicy,
        )
        if (allowedByPolicy.isEmpty()) {
            request.deny()
            return
        }

        if (!manifestPermissionsDeclared(allowedByPolicy)) {
            request.deny()
            return
        }

        val runtimeNeeded = WebViewPermissionLogic.runtimePermissionsFor(allowedByPolicy)
            .filter { !isGranted(it) }
        if (runtimeNeeded.isEmpty()) {
            request.grant(allowedByPolicy)
            return
        }

        pendingRequest?.deny()
        pendingRequest = request
        permissionLauncher.launch(runtimeNeeded.toTypedArray())
    }

    private fun respondToRequest(
        request: PermissionRequest,
        grantResults: Map<String, Boolean>,
    ) {
        val requested = request.resources ?: emptyArray()
        val grantable = WebViewPermissionLogic.grantableResourcesAfterRuntimeResult(
            requested = requested,
            cameraPolicy = cameraPolicy,
            microphonePolicy = microphonePolicy,
            isGranted = { permission ->
                grantResults[permission] == true || isGranted(permission)
            },
        )
        if (grantable.isEmpty()) {
            request.deny()
        } else {
            request.grant(grantable)
        }
    }

    private fun manifestPermissionsDeclared(webResources: Array<String>): Boolean {
        val declared = declaredManifestPermissions()
        return webResources.flatMap { WebViewPermissionLogic.manifestPermissionsFor(it) }
            .distinct()
            .all { it in declared }
    }

    private fun declaredManifestPermissions(): Set<String> {
        return try {
            val info = activity.packageManager.getPackageInfo(
                activity.packageName,
                PackageManager.GET_PERMISSIONS,
            )
            info.requestedPermissions?.toSet() ?: emptySet()
        } catch (_: PackageManager.NameNotFoundException) {
            emptySet()
        }
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            activity,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
}

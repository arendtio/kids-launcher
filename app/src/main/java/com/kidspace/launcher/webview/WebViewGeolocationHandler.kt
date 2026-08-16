package com.kidspace.launcher.webview

import android.Manifest
import android.content.pm.PackageManager
import android.webkit.GeolocationPermissions
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.kidspace.launcher.data.model.PermissionPolicy

/**
 * Handles [WebChromeClient.onGeolocationPermissionsShowPrompt] with parent policy
 * and Android runtime location permissions.
 */
class WebViewGeolocationHandler(
    private val activity: ComponentActivity,
    private val locationPolicy: PermissionPolicy,
) {
    private var pendingOrigin: String? = null
    private var pendingCallback: GeolocationPermissions.Callback? = null

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val locationLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        val origin = pendingOrigin
        val callback = pendingCallback
        pendingOrigin = null
        pendingCallback = null
        activity.runOnUiThread {
            respondToCallback(origin, callback)
        }
    }

    fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?,
    ) {
        activity.runOnUiThread {
            if (locationPolicy != PermissionPolicy.GRANT) {
                callback?.invoke(origin, false, false)
                return@runOnUiThread
            }

            if (hasLocationPermission()) {
                callback?.invoke(origin, true, false)
                return@runOnUiThread
            }

            pendingOrigin = origin
            pendingCallback = callback
            locationLauncher.launch(locationPermissions)
        }
    }

    fun cancel() {
        pendingCallback?.invoke(pendingOrigin, false, false)
        pendingOrigin = null
        pendingCallback = null
    }

    private fun respondToCallback(
        origin: String?,
        callback: GeolocationPermissions.Callback?,
    ) {
        if (locationPolicy == PermissionPolicy.GRANT && hasLocationPermission()) {
            callback?.invoke(origin, true, false)
        } else {
            callback?.invoke(origin, false, false)
        }
    }

    private fun hasLocationPermission(): Boolean =
        locationPermissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
}

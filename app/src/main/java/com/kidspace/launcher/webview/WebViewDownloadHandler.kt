package com.kidspace.launcher.webview

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import com.kidspace.launcher.data.model.PermissionPolicy

/**
 * Handles [WebViewClient.onDownloadStart] when the parent has allowed downloads for the link.
 */
class WebViewDownloadHandler(
    private val context: Context,
    private val downloadPolicy: PermissionPolicy,
) {
    fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long,
    ) {
        if (downloadPolicy != PermissionPolicy.GRANT || url.isNullOrBlank()) return

        val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimeType)
            setTitle(filename)
            setDescription("Download from KidSpace browser")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            userAgent?.let { addRequestHeader("User-Agent", it) }
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}

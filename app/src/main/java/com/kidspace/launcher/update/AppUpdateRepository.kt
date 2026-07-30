package com.kidspace.launcher.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppUpdateRepository(
    private val context: Context,
) {
    suspend fun downloadLatestApk(): File = withContext(Dispatchers.IO) {
        val connection = (URL(AppUpdateConfig.DEBUG_APK_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 120_000
            instanceFollowRedirects = true
            requestMethod = "GET"
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                error("Download failed with HTTP $responseCode")
            }

            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val outputFile = File(updatesDir, AppUpdateConfig.APK_FILE_NAME)

            connection.inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (!outputFile.exists() || outputFile.length() == 0L) {
                error("Downloaded file is empty")
            }

            outputFile
        } finally {
            connection.disconnect()
        }
    }
}

object AppUpdateInstaller {
    fun install(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

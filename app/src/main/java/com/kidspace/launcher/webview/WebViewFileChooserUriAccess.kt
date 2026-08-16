package com.kidspace.launcher.webview

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

object WebViewFileChooserUriAccess {
    fun normalizeAcceptTypes(acceptTypes: Array<String>?): Array<String> {
        val normalized = linkedSetOf<String>()
        acceptTypes?.forEach { raw ->
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return@forEach
            when {
                trimmed.startsWith(".") -> addExtensionMime(normalized, trimmed)
                trimmed.contains("/") -> normalized.add(trimmed)
                else -> addExtensionMime(normalized, ".$trimmed")
            }
        }
        return if (normalized.isEmpty()) {
            arrayOf("*/*")
        } else {
            normalized.toTypedArray()
        }
    }

    fun prepareForWebView(context: Context, uris: Array<Uri>): Array<Uri> {
        return uris.map { prepareSingle(context, it) }.toTypedArray()
    }

    private fun prepareSingle(context: Context, uri: Uri): Uri {
        val authority = "${context.packageName}.fileprovider"
        if (uri.authority == authority) {
            return uri
        }
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri).orEmpty()
        val extension = extensionForUri(uri, mimeType)
        val destination = File(
            context.cacheDir,
            "upload_${System.currentTimeMillis()}.$extension",
        )
        val copied = runCatching {
            resolver.openInputStream(uri)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Missing input stream")
            FileProvider.getUriForFile(context, authority, destination)
        }
        return copied.getOrElse { uri }
    }

    private fun addExtensionMime(target: LinkedHashSet<String>, extensionWithDot: String) {
        val extension = extensionWithDot.removePrefix(".").lowercase()
        val mapped = knownExtensionMime(extension)
            ?: platformExtensionMime(extension)
        if (mapped != null) {
            target.add(mapped)
        }
        if (extension == "zip") {
            target.add("application/octet-stream")
        }
    }

    private fun platformExtensionMime(extension: String): String? = runCatching {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }.getOrNull()

    private fun knownExtensionMime(extension: String): String? = when (extension) {
        "zip" -> "application/zip"
        "json" -> "application/json"
        "db" -> "application/octet-stream"
        else -> null
    }

    private fun extensionForUri(uri: Uri, mimeType: String): String {
        knownMimeExtension(mimeType)?.let { return it }
        platformMimeExtension(mimeType)?.let { return it }
        val name = uri.lastPathSegment.orEmpty()
        val fromName = name.substringAfterLast('.', "")
        if (fromName.isNotBlank()) return fromName.lowercase()
        return "bin"
    }

    private fun platformMimeExtension(mimeType: String): String? = runCatching {
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }.getOrNull()

    private fun knownMimeExtension(mimeType: String): String? = when (mimeType) {
        "application/zip" -> "zip"
        "application/json" -> "json"
        else -> null
    }
}

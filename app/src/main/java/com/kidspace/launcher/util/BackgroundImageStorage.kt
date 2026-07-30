package com.kidspace.launcher.util

import android.content.Context
import android.net.Uri
import java.io.File

object BackgroundImageStorage {
    private const val BASENAME = "custom_background"

    fun saveFromUri(context: Context, sourceUri: Uri): String {
        val mime = context.contentResolver.getType(sourceUri).orEmpty()
        val extension = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        clear(context)
        val target = File(context.filesDir, "$BASENAME.$extension")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not read selected image")
        return target.absolutePath
    }

    fun clear(context: Context) {
        context.filesDir.listFiles()
            ?.filter { it.name.startsWith(BASENAME) }
            ?.forEach { it.delete() }
    }

    fun existingPath(context: Context): String? =
        context.filesDir.listFiles()
            ?.firstOrNull { it.name.startsWith(BASENAME) }
            ?.absolutePath
}

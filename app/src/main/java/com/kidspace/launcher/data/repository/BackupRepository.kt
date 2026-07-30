package com.kidspace.launcher.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.BackgroundType
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.domain.BackupCodec
import com.kidspace.launcher.domain.KidSpaceBackup
import com.kidspace.launcher.util.BackgroundImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class BackupRepository(
    private val context: Context,
    private val tileRepository: TileRepository,
    private val appearanceRepository: AppearanceRepository,
) {
    suspend fun createBackup(): KidSpaceBackup = withContext(Dispatchers.IO) {
        val tiles = tileRepository.getAllTiles()
        val appearance = appearanceRepository.observeSettings().first()
        val (base64, mime) = readCustomBackground(appearance)
        KidSpaceBackup(
            tiles = tiles,
            appearance = appearance,
            customBackgroundBase64 = base64,
            customBackgroundMimeType = mime,
        )
    }

    suspend fun exportTo(uri: Uri) = withContext(Dispatchers.IO) {
        val backup = createBackup()
        val json = BackupCodec.encode(backup)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray(Charsets.UTF_8))
        } ?: error("Could not write backup file")
    }

    suspend fun importFrom(uri: Uri) = withContext(Dispatchers.IO) {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: error("Could not read backup file")

        val backup = BackupCodec.decode(json)
        tileRepository.replaceAllTiles(backup.tiles)

        val appearance = resolveAppearance(backup)
        appearanceRepository.saveSettings(appearance)
    }

    private fun readCustomBackground(appearance: AppearanceSettings): Pair<String?, String?> {
        if (appearance.backgroundType != BackgroundType.CUSTOM) return null to null
        val path = appearance.customBackgroundUri ?: return null to null
        val file = File(path)
        if (!file.exists()) return null to null
        val mime = when (file.extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val encoded = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        return encoded to mime
    }

    private fun resolveAppearance(backup: KidSpaceBackup): AppearanceSettings {
        val base = backup.appearance
        val encoded = backup.customBackgroundBase64
        val mime = backup.customBackgroundMimeType
        if (base.backgroundType != BackgroundType.CUSTOM || encoded.isNullOrBlank() || mime.isNullOrBlank()) {
            return base.copy(customBackgroundUri = null)
        }

        BackgroundImageStorage.clear(context)
        val extension = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            else -> "jpg"
        }
        val target = File(context.filesDir, "custom_background.$extension")
        target.writeBytes(Base64.decode(encoded, Base64.DEFAULT))
        return base.copy(customBackgroundUri = target.absolutePath)
    }
}

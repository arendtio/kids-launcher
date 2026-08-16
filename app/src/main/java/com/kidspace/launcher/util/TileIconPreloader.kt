package com.kidspace.launcher.util

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.TileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TileIconPreloader {

    const val EagerLoadTileLimit = 100

    fun remoteImageUrl(iconKey: String, target: String): String? {
        return when {
            iconKey.startsWith("youtube:") -> IconKeyGenerator.youtubeThumbnailUrl(iconKey)
            iconKey.startsWith("http") && !SiteIconResolver.isGoogleFaviconFallback(iconKey) -> iconKey
            iconKey.startsWith("favicon:") -> null
            else -> null
        }
    }

    suspend fun preloadTiles(context: Context, tiles: List<ChildTile>) {
        if (tiles.isEmpty()) return
        withContext(Dispatchers.IO) {
            val loader = context.imageLoader
            tiles.forEach { tile ->
                val url = remoteImageUrl(tile.iconKey, tile.target) ?: return@forEach
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(false)
                    .build()
                runCatching { loader.execute(request) }
            }
        }
    }
}

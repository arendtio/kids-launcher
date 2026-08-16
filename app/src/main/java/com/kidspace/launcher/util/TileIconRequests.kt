package com.kidspace.launcher.util

import android.content.Context
import coil.request.ImageRequest
import coil.size.Scale

/**
 * Builds Coil requests that decode tile artwork once at grid display size.
 * Memory cache retains the downscaled bitmap for the app session.
 */
object TileIconRequests {

    /** ~96dp icon slot at 2x density; enough for crisp tiles without decoding 512px sources. */
    const val DECODE_SIZE_PX = 192

    fun fromUrl(context: Context, url: String): ImageRequest =
        fromData(context, data = url, cacheKey = url)

    fun fromData(context: Context, data: Any, cacheKey: String? = null): ImageRequest {
        val key = cacheKey ?: data.toString()
        return ImageRequest.Builder(context)
            .data(data)
            .size(DECODE_SIZE_PX)
            .scale(Scale.FIT)
            .crossfade(false)
            .memoryCacheKey("$key@$DECODE_SIZE_PX")
            .build()
    }
}

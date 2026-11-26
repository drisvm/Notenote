package com.notenote.app.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.ceil
import kotlin.random.Random

/**
 * Generates a small, tileable paper-grain bitmap once and caches it. The texture is drawn
 * as a lightweight tiled overlay instead of any real-time shader work.
 */
object PaperTextureFactory {

    private const val TILE_SIZE = 144

    @Volatile
    private var cachedLight: ImageBitmap? = null
    private var cachedDark: ImageBitmap? = null

    fun light(): ImageBitmap = cachedLight ?: create(seed = 0x5041, baseAlpha = 26).also { cachedLight = it }
    fun dark(): ImageBitmap = cachedDark ?: create(seed = 0x7042, baseAlpha = 30).also { cachedDark = it }

    private fun create(seed: Long, baseAlpha: Int): ImageBitmap {
        val bitmap = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888)
        val random = Random(seed)
        val pixels = IntArray(TILE_SIZE * TILE_SIZE)
        for (y in 0 until TILE_SIZE) {
            for (x in 0 until TILE_SIZE) {
                val brightness = random.nextInt(256)
                val alpha = (baseAlpha + random.nextInt(baseAlpha)).coerceAtMost(255)
                pixels[y * TILE_SIZE + x] = (alpha shl 24) or
                    (brightness shl 16) or (brightness shl 8) or brightness
            }
        }
        bitmap.setPixels(pixels, 0, TILE_SIZE, 0, 0, TILE_SIZE, TILE_SIZE)
        return bitmap.asImageBitmap()
    }
}

fun Modifier.paperTexture(
    enabled: Boolean,
    darkTheme: Boolean,
): Modifier {
    if (!enabled) return this
    val bitmap = if (darkTheme) PaperTextureFactory.dark() else PaperTextureFactory.light()
    val alpha = if (darkTheme) 0.20f else 0.30f
    return this.drawBehind { drawTiled(bitmap, alpha) }
}

private fun DrawScope.drawTiled(bitmap: ImageBitmap, alpha: Float) {
    if (bitmap.width == 0 || bitmap.height == 0) return
    val columns = ceil(size.width / bitmap.width).toInt().coerceAtLeast(1)
    val rows = ceil(size.height / bitmap.height).toInt().coerceAtLeast(1)
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            drawImage(
                image = bitmap,
                dstOffset = IntOffset(
                    x = column * bitmap.width,
                    y = row * bitmap.height,
                ),
                alpha = alpha,
            )
        }
    }
}

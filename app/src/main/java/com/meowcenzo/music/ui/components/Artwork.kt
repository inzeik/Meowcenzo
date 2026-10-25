package com.meowcenzo.music.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape // ADDED IMPORT
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.meowcenzo.music.data.Song
import com.meowcenzo.music.ui.theme.Meow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/** Memory cache + loader for album art. No network, no third-party image library. */
object ArtworkCache {
    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 10).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }
    private val misses = HashSet<String>()

    suspend fun load(context: Context, song: Song, size: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = "${song.artKey}:$size"
        cache.get(key)?.let { return@withContext it }
        if (synchronized(misses) { key in misses }) return@withContext null
        val bmp = decode(context, song, size)
        if (bmp != null) cache.put(key, bmp) else synchronized(misses) { misses.add(key) }
        bmp
    }

    private fun decode(context: Context, song: Song, size: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                return context.contentResolver.loadThumbnail(song.uri, Size(size, size), null)
            } catch (_: Exception) {
                // fall through to embedded picture
            }
        }
        return try {
            val r = MediaMetadataRetriever()
            try {
                r.setDataSource(context, song.uri)
                val bytes = r.embeddedPicture ?: return null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth > 0 && bounds.outWidth / (sample * 2) >= size) sample *= 2
                BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.size,
                    BitmapFactory.Options().apply { inSampleSize = sample }
                )
            } finally {
                r.release()
            }
        } catch (_: Exception) {
            null
        }
    }
}

/** Average colour of the artwork, pulled into a range that looks good behind white text. */
fun Bitmap.accentColor(): Color {
    val src = if (config == Bitmap.Config.HARDWARE) copy(Bitmap.Config.ARGB_8888, false) else this
    val px = Bitmap.createScaledBitmap(src, 1, 1, true).getPixel(0, 0)
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(px, hsv)
    hsv[1] = hsv[1].coerceIn(0.35f, 0.85f)
    hsv[2] = hsv[2].coerceIn(0.35f, 0.62f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

private fun placeholderBrush(key: Long): Brush {
    val hue = (abs(key * 47L) % 360L).toFloat()
    val a = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.45f, 0.50f)))
    val b = Color(android.graphics.Color.HSVToColor(floatArrayOf((hue + 40f) % 360f, 0.55f, 0.30f)))
    return Brush.linearGradient(listOf(a, b))
}

@Composable
fun ArtworkImage(
    song: Song?,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape, // CHANGED: Default is now a perfect square
    px: Int = 512,                 // CHANGED: Increased default resolution for better quality
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, song?.artKey, px) {
        value = if (song == null) null else ArtworkCache.load(context, song, px)
    }
    val placeholder = remember(song?.artKey) { placeholderBrush(song?.artKey ?: 0L) }
    Box(
        modifier.clip(shape).background(placeholder),
        contentAlignment = Alignment.Center,
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxSize(0.4f),
            )
        }
    }
}
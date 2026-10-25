package com.meowcenzo.music.data

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads every music file on the device from MediaStore. Works fully offline. */
object MediaStoreScanner {

    suspend fun scan(context: Context, minDurationMs: Long = 20_000L): List<Song> =
        withContext(Dispatchers.IO) {
            val out = ArrayList<Song>()
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.TRACK,
            )
            val selection =
                "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
            val args = arrayOf(minDurationMs.toString())
            val order = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

            try {
                context.contentResolver.query(collection, projection, selection, args, order)?.use { c ->
                    val idC = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumIdC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val addedC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                    val trackC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

                    while (c.moveToNext()) {
                        val id = c.getLong(idC)
                        out.add(
                            Song(
                                id = id,
                                title = clean(c.getString(titleC), "Unknown title"),
                                artist = clean(c.getString(artistC), "Unknown artist"),
                                album = clean(c.getString(albumC), "Unknown album"),
                                albumId = c.getLong(albumIdC),
                                durationMs = c.getLong(durC),
                                uri = ContentUris.withAppendedId(collection, id),
                                path = c.getString(dataC).orEmpty(),
                                dateAddedSec = c.getLong(addedC),
                                track = c.getInt(trackC) % 1000, // MediaStore stores disc*1000 + track
                            )
                        )
                    }
                }
            } catch (_: SecurityException) {
                // Permission was revoked while scanning; return what we have.
            }
            out
        }

    private fun clean(value: String?, fallback: String): String =
        if (value.isNullOrBlank() || value == "<unknown>") fallback else value
}

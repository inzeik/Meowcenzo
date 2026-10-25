package com.meowcenzo.music.data

import android.content.ContentUris
import android.net.Uri
import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: Uri,
    val path: String,
    val dateAddedSec: Long,
    val track: Int,
) {
    /** Key used to cache artwork: one image per album when possible. */
    val artKey: Long get() = if (albumId > 0) albumId else -id

    val albumArtUri: Uri
        get() = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)

    val folderPath: String get() = File(path).parent ?: "Unknown"
    val folderName: String get() = File(folderPath).name.ifBlank { folderPath }
}

@Immutable
data class Album(val id: Long, val name: String, val artist: String, val songs: List<Song>)

@Immutable
data class Artist(val name: String, val songs: List<Song>) {
    val albumCount: Int get() = songs.map { it.albumId }.distinct().size
}

@Immutable
data class Folder(val path: String, val name: String, val songs: List<Song>)

@Immutable
data class Playlist(val id: String, val name: String, val songIds: List<Long>)

enum class SortOrder(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    RECENT("Recently added"),
    DURATION("Longest first"),
}

package com.example.meowcenzo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meowcenzo.data.Album
import com.example.meowcenzo.data.Artist
import com.example.meowcenzo.data.Folder
import com.example.meowcenzo.data.LibraryStore
import com.example.meowcenzo.data.MediaStoreScanner
import com.example.meowcenzo.data.Playlist
import com.example.meowcenzo.data.Song
import com.example.meowcenzo.data.SortOrder
import com.example.meowcenzo.playback.EqualizerManager
import com.example.meowcenzo.playback.PlayerController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MusicViewModel(app: Application) : AndroidViewModel(app) {

    private val store = LibraryStore(app)
    val player = PlayerController(app)

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val query = MutableStateFlow("")
    val sort = MutableStateFlow(SortOrder.TITLE)

    private val _favorites = MutableStateFlow(store.favorites())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()

    private val _playlists = MutableStateFlow(store.playlists())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _recents = MutableStateFlow(store.recents())
    private val _counts = MutableStateFlow(store.playCounts())

    private fun <T> Flow<T>.hot(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.Eagerly, initial)

    val songMap: StateFlow<Map<Long, Song>> =
        _songs.map { l -> l.associateBy { it.id } }.hot(emptyMap())

    val visibleSongs: StateFlow<List<Song>> = combine(_songs, query, sort) { list, q, order ->
        val filtered = if (q.isBlank()) list else list.filter {
            it.title.contains(q, true) || it.artist.contains(q, true) || it.album.contains(q, true)
        }
        when (order) {
            SortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> filtered.sortedWith(
                compareBy<Song>({ it.artist.lowercase() }, { it.album.lowercase() }, { it.track })
            )
            SortOrder.RECENT -> filtered.sortedByDescending { it.dateAddedSec }
            SortOrder.DURATION -> filtered.sortedByDescending { it.durationMs }
        }
    }.hot(emptyList())

    val albums: StateFlow<List<Album>> = combine(_songs, query) { list, q ->
        list.groupBy { it.albumId to it.album }
            .map { (key, ss) ->
                Album(key.first, key.second, ss.first().artist, ss.sortedBy { it.track })
            }
            .filter { q.isBlank() || it.name.contains(q, true) || it.artist.contains(q, true) }
            .sortedBy { it.name.lowercase() }
    }.hot(emptyList())

    val artists: StateFlow<List<Artist>> = combine(_songs, query) { list, q ->
        list.groupBy { it.artist }
            .map { (name, ss) -> Artist(name, ss.sortedBy { it.album.lowercase() }) }
            .filter { q.isBlank() || it.name.contains(q, true) }
            .sortedBy { it.name.lowercase() }
    }.hot(emptyList())

    val folders: StateFlow<List<Folder>> = _songs.map { list ->
        list.groupBy { it.folderPath }
            .map { (path, ss) -> Folder(path, ss.first().folderName, ss.sortedBy { it.title.lowercase() }) }
            .sortedBy { it.name.lowercase() }
    }.hot(emptyList())

    val favoriteSongs: StateFlow<List<Song>> = combine(_songs, _favorites) { list, favs ->
        list.filter { it.id in favs }
    }.hot(emptyList())

    val recentSongs: StateFlow<List<Song>> = combine(songMap, _recents) { map, ids ->
        ids.mapNotNull { map[it] }
    }.hot(emptyList())

    val mostPlayed: StateFlow<List<Song>> = combine(songMap, _counts) { map, counts ->
        counts.entries.sortedByDescending { it.value }.take(50).mapNotNull { map[it.key] }
    }.hot(emptyList())

    init {
        EqualizerManager.init(app)
        player.onTrackStarted = ::markPlayed
        player.connect()
    }

    fun scan() {
        viewModelScope.launch {
            _loading.value = true
            _songs.value = MediaStoreScanner.scan(getApplication())
            _loading.value = false
        }
    }

    // ---- Playback shortcuts ------------------------------------------------------------------

    fun play(list: List<Song>, index: Int) = player.playQueue(list, index)
    fun shuffle(list: List<Song>) = player.playQueue(list, 0, shuffle = true)

    private fun markPlayed(id: Long) {
        val recents = (listOf(id) + _recents.value.filter { it != id }).take(30)
        _recents.value = recents
        store.saveRecents(recents)
        val counts = _counts.value.toMutableMap().also { it[id] = (it[id] ?: 0) + 1 }
        _counts.value = counts
        store.savePlayCounts(counts)
    }

    // ---- Favourites --------------------------------------------------------------------------

    fun toggleFavorite(id: Long) {
        val next = _favorites.value.let { if (id in it) it - id else it + id }
        _favorites.value = next
        store.saveFavorites(next)
    }

    // ---- Playlists ---------------------------------------------------------------------------

    private fun savePlaylists(list: List<Playlist>) {
        _playlists.value = list
        store.savePlaylists(list)
    }

    fun createPlaylist(name: String, firstSongId: Long? = null) {
        val clean = name.trim().ifBlank { return }
        savePlaylists(_playlists.value + Playlist(UUID.randomUUID().toString(), clean, listOfNotNull(firstSongId)))
    }

    fun addToPlaylist(playlistId: String, songId: Long) {
        savePlaylists(_playlists.value.map {
            if (it.id == playlistId && songId !in it.songIds) it.copy(songIds = it.songIds + songId) else it
        })
    }

    fun removeFromPlaylist(playlistId: String, songId: Long) {
        savePlaylists(_playlists.value.map {
            if (it.id == playlistId) it.copy(songIds = it.songIds - songId) else it
        })
    }

    fun deletePlaylist(playlistId: String) {
        savePlaylists(_playlists.value.filterNot { it.id == playlistId })
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}

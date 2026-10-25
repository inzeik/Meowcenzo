package com.example.meowcenzo.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Tiny offline persistence layer: favourites, playlists, history and play counts. */
class LibraryStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("meowcenzo_library", Context.MODE_PRIVATE)

    fun favorites(): Set<Long> = readLongs("favorites").toSet()
    fun saveFavorites(v: Set<Long>) = writeLongs("favorites", v.toList())

    fun recents(): List<Long> = readLongs("recents")
    fun saveRecents(v: List<Long>) = writeLongs("recents", v)

    fun playCounts(): Map<Long, Int> =
        prefs.getString("counts", "").orEmpty()
            .split(',')
            .mapNotNull { pair ->
                val p = pair.split(':')
                val id = p.getOrNull(0)?.toLongOrNull()
                val n = p.getOrNull(1)?.toIntOrNull()
                if (id != null && n != null) id to n else null
            }.toMap()

    fun savePlayCounts(m: Map<Long, Int>) {
        prefs.edit().putString("counts", m.entries.joinToString(",") { "${it.key}:${it.value}" }).apply()
    }

    fun playlists(): List<Playlist> = try {
        val arr = JSONArray(prefs.getString("playlists", "[]"))
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val ids = o.getJSONArray("songs")
            Playlist(o.getString("id"), o.getString("name"), List(ids.length()) { ids.getLong(it) })
        }
    } catch (_: Exception) {
        emptyList()
    }

    fun savePlaylists(list: List<Playlist>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("songs", JSONArray(p.songIds))
            )
        }
        prefs.edit().putString("playlists", arr.toString()).apply()
    }

    private fun readLongs(key: String): List<Long> =
        prefs.getString(key, "").orEmpty().split(',').mapNotNull { it.toLongOrNull() }

    private fun writeLongs(key: String, list: List<Long>) {
        prefs.edit().putString(key, list.joinToString(",")).apply()
    }
}

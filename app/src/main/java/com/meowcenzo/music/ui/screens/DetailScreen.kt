package com.meowcenzo.music.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meowcenzo.music.MusicViewModel
import com.meowcenzo.music.data.Song
import com.meowcenzo.music.ui.components.ArtworkImage
import com.meowcenzo.music.ui.components.BigTileButton
import com.meowcenzo.music.ui.components.RectButton
import com.meowcenzo.music.ui.components.SongRow
import com.meowcenzo.music.ui.theme.Meow

/** One screen reused for album, artist, folder, playlist and "most played" detail pages. */
@Composable
fun DetailScreen(
    vm: MusicViewModel,
    title: String,
    subtitle: String,
    songs: List<Song>,
    bottomPad: Dp,
    onBack: () -> Unit,
    onSongMore: (Song) -> Unit,
    onDeleteList: (() -> Unit)? = null,
) {
    val ui by vm.player.ui.collectAsState()
    val favs by vm.favorites.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RectButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, filled = true, description = "Back")
            Spacer(Modifier.weight(1f))
            if (onDeleteList != null) {
                RectButton(Icons.Rounded.Delete, onDeleteList, tint = Meow.Coral, description = "Delete playlist")
            }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = bottomPad)) {
            item(key = "header") {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ArtworkImage(songs.firstOrNull(), Modifier.size(220.dp), Meow.TileLarge, px = 600)
                    Spacer(Modifier.height(18.dp))
                    Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, maxLines = 2)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Meow.TextLo)
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BigTileButton("Play", Icons.Rounded.PlayArrow, { vm.play(songs, 0) }, Modifier.weight(1f), primary = true)
                        BigTileButton("Shuffle", Icons.Rounded.Shuffle, { vm.shuffle(songs) }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            itemsIndexed(songs, key = { i, s -> "$i:${s.id}" }) { i, s ->
                SongRow(
                    song = s,
                    isCurrent = ui.songId == s.id,
                    isPlaying = ui.isPlaying,
                    isFavorite = s.id in favs,
                    onClick = { vm.play(songs, i) },
                    onMore = { onSongMore(s) },
                )
            }
        }
    }
}

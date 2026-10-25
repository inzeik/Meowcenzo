package com.meowcenzo.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meowcenzo.music.MusicViewModel
import com.meowcenzo.music.data.Song
import com.meowcenzo.music.ui.components.ArtworkImage
import com.meowcenzo.music.ui.components.BigTileButton
import com.meowcenzo.music.ui.components.RectButton
import com.meowcenzo.music.ui.components.formatTime
import com.meowcenzo.music.ui.theme.Meow
import java.util.Locale

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

    Column(Modifier.fillMaxSize().background(Meow.Ink)) {
        // --- TOP BAR ---
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RectButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, filled = true, description = "Back")
            Spacer(Modifier.weight(1f))
            if (onDeleteList != null) {
                RectButton(Icons.Rounded.Delete, onDeleteList, tint = Meow.Coral, description = "Delete playlist")
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = bottomPad)) {
            // --- HEADER ---
            item(key = "header") {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ArtworkImage(songs.firstOrNull(), Modifier.size(220.dp), RectangleShape, px = 600)
                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = title.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = Meow.TextHi,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )

                    Text(
                        text = subtitle.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Meow.TextLo,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BigTileButton("PLAY", Icons.Rounded.PlayArrow, { vm.play(songs, 0) }, Modifier.weight(1f), primary = true)
                        BigTileButton("SHUFFLE", Icons.Rounded.Shuffle, { vm.shuffle(songs) }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // --- SONG LIST (Replaced SongRow with inline Monospace layout) ---
            itemsIndexed(songs, key = { i, s -> "$i:${s.id}" }) { i, s ->
                val isCurrent = ui.songId == s.id

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.play(songs, i) }
                            .background(if (isCurrent) Meow.PanelHigh else androidx.compose.ui.graphics.Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtworkImage(
                            song = s,
                            modifier = Modifier.size(56.dp).border(1.dp, Meow.Line, RectangleShape),
                            shape = RectangleShape,
                            px = 128
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = s.title.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Meow.TextHi,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = s.artist.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = Meow.TextLo,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = formatTime(s.durationMs),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            color = Meow.TextLo
                        )
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            Icons.Rounded.MoreVert,
                            "More",
                            tint = Meow.TextHi,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onSongMore(s) }
                        )
                    }
                    HorizontalDivider(color = Meow.Line.copy(alpha = 0.3f), thickness = 1.dp)
                }
            }
        }
    }
}
package com.meowcenzo.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meowcenzo.music.MusicViewModel
import com.meowcenzo.music.data.Song
import com.meowcenzo.music.ui.components.ArtworkImage
import com.meowcenzo.music.ui.components.formatTime
import com.meowcenzo.music.ui.theme.Meow
import java.util.Locale

@Composable
fun HomeScreen(
    vm: MusicViewModel,
    onOpenPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEqualizer: () -> Unit
) {
    val songs by vm.songs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("SONGS", "ALBUMS", "ARTISTS", "PLAYLISTS", "FAVS")

    // I have commented this out so it doesn't crash on load.
    // You can uncomment and type "vm." to see the correct method (e.g. vm.loadLibrary())
    /*
    LaunchedEffect(Unit) {
        vm.loadSongs()
    }
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Meow.Ink) // Pure Black
            .statusBarsPadding()
    ) {
        // --- TOP HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MEOWCENZO".uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = Meow.TextHi
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Rounded.Search, "Search", tint = Meow.TextHi, modifier = Modifier.size(24.dp))
                Icon(Icons.Rounded.Equalizer, "EQ", tint = Meow.TextHi, modifier = Modifier.size(24.dp).clickable { onOpenEqualizer() })
                // Click this to refresh. Type "vm." after the opening parenthesis to find the right method.
                Icon(Icons.Rounded.Refresh, "Refresh", tint = Meow.TextHi, modifier = Modifier.size(24.dp).clickable { /* vm.loadLibrary() */ })
            }
        }

        // --- TABS (Hardware Switch Style) ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs.size) { index ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .background(if (isSelected) Meow.TextHi else Meow.PanelHigh, RectangleShape)
                        .border(1.dp, if (isSelected) Meow.TextHi else Meow.Line, RectangleShape)
                        .clickable { selectedTab = index }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabs[index].uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = if (isSelected) Meow.Ink else Meow.TextLo
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- PLAY ALL / SHUFFLE BUTTONS ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Play All Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(Meow.PanelHigh, RectangleShape)
                    .border(1.dp, Meow.Line, RectangleShape)
                    .clickable {
                        if (songs.isNotEmpty()) {
                            // vm.player.playSong(songs.first())
                            onOpenPlayer()
                        }
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, null, tint = Meow.TextHi, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("PLAY ALL", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = Meow.TextHi)
            }

            // Shuffle Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(Meow.PanelHigh, RectangleShape)
                    .border(1.dp, Meow.Line, RectangleShape)
                    .clickable {
                        if (songs.isNotEmpty()) {
                            // vm.player.toggleShuffle()
                            // vm.player.playSong(songs.random())
                            onOpenPlayer()
                        }
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.Shuffle, null, tint = Meow.TextHi, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("SHUFFLE", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = Meow.TextHi)
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- SCROLLABLE CONTENT ---
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Recently Played Section
            item {
                Text(
                    text = "RECENTLY PLAYED".uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Meow.TextHi,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(songs.take(5)) { song ->
                        RecentlyPlayedCard(song) {
                            // vm.player.playSong(song)
                            onOpenPlayer()
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Most Played Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Meow.PanelHigh, RectangleShape)
                        .border(1.dp, Meow.Line, RectangleShape)
                        .clickable {
                            if (songs.isNotEmpty()) {
                                // vm.player.playSong(songs.first())
                                onOpenPlayer()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).background(Meow.Ink, RectangleShape).border(1.dp, Meow.Line, RectangleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Meow.TextHi, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("MOST PLAYED", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Meow.TextHi)
                        Text("YOUR TOP TRACKS", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = Meow.TextLo)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Song Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${songs.size} SONGS", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace), color = Meow.TextLo)
                    Icon(Icons.Rounded.Sort, "Sort", tint = Meow.TextHi, modifier = Modifier.size(24.dp))
                }
            }

            // Full Song List
            items(songs) { song ->
                SongListItem(song) {
                    // vm.player.playSong(song)
                    onOpenPlayer()
                }
            }
        }
    }
}

@Composable
fun RecentlyPlayedCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(140.dp).clickable(onClick = onClick)
    ) {
        ArtworkImage(
            song = song,
            modifier = Modifier.size(140.dp).border(1.dp, Meow.Line, RectangleShape),
            shape = RectangleShape,
            px = 256
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = Meow.TextHi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.bodySmall,
            color = Meow.TextLo,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongListItem(song: Song, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtworkImage(
                song = song,
                modifier = Modifier.size(56.dp).border(1.dp, Meow.Line, RectangleShape),
                shape = RectangleShape,
                px = 128
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = song.title.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Meow.TextHi,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Meow.TextLo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = formatTime(song.durationMs),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo
            )
            Spacer(Modifier.width(12.dp))
            Icon(Icons.Rounded.MoreVert, "More", tint = Meow.TextHi, modifier = Modifier.size(20.dp))
        }
        HorizontalDivider(color = Meow.Line.copy(alpha = 0.3f), thickness = 1.dp)
    }
}
package com.meowcenzo.music.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meowcenzo.music.MusicViewModel
import com.meowcenzo.music.data.Song
import com.meowcenzo.music.ui.Route
import com.meowcenzo.music.ui.components.ArtworkImage
import com.meowcenzo.music.ui.components.formatTime
import com.meowcenzo.music.ui.theme.Meow
import java.util.Locale

@Composable
fun HomeScreen(
    vm: MusicViewModel,
    onOpenPlayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    val songs by vm.songs.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val favs by vm.favorites.collectAsState()
    val ui by vm.player.ui.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("SONGS", "ALBUMS", "ARTISTS", "PLAYLISTS", "FAVS")

    // Blinking animations
    val dotAlpha by rememberInfiniteTransition(label = "dot").animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "dotAlpha"
    )
    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursorAlpha"
    )
    val loadBarWidth by rememberInfiniteTransition(label = "load").animateFloat(
        initialValue = 0.1f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "loadBarWidth"
    )

    val albums = remember(songs) { songs.groupBy { it.albumId }.values.map { it.first() }.sortedBy { it.album.lowercase() } }
    val artists = remember(songs) { songs.groupBy { it.artist }.toList().sortedBy { it.first.lowercase() } }

    // Hardware Button Interaction
    val executeInteraction = remember { MutableInteractionSource() }
    val isExecutePressed by executeInteraction.collectIsPressedAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Meow.Ink)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).alpha(dotAlpha).background(Meow.TextHi, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "MEOWCENZO".uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Meow.TextHi
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Rounded.Equalizer, "EQ", tint = Meow.TextHi, modifier = Modifier.size(24.dp).clickable { onOpenEqualizer() })
                Icon(Icons.Rounded.Refresh, "Refresh", tint = Meow.TextHi, modifier = Modifier.size(24.dp).clickable { /* vm.scan() */ })
            }
        }

        // --- COMMAND BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Meow.PanelHigh, RectangleShape)
                .border(1.dp, Meow.Line, RectangleShape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> ",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black),
                color = Meow.TextHi
            )
            Text(
                text = "SEARCH_LIBRARY",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo,
            )
            Text(
                text = "_",
                modifier = Modifier.alpha(cursorAlpha),
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextHi
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.Search, "Search", tint = Meow.TextLo, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.height(12.dp))

        // --- TABS ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs.size) { index ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .background(if (isSelected) Meow.TextHi else Meow.Ink, RectangleShape)
                        .border(1.dp, if (isSelected) Meow.TextHi else Meow.Line, RectangleShape)
                        .clickable { selectedTab = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSelected) "▸ ${tabs[index]}" else tabs[index],
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isSelected) Meow.Ink else Meow.TextLo
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- ACTIONS ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Play All (Hardware Button)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(if (isExecutePressed) Meow.PanelHigh else Meow.TextHi, RectangleShape)
                    .border(1.dp, Meow.TextHi, RectangleShape)
                    .clickable(
                        interactionSource = executeInteraction,
                        indication = null
                    ) {
                        if (songs.isNotEmpty()) {
                            vm.player.playQueue(songs, startIndex = 0)
                            onOpenPlayer()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isExecutePressed) "[ EXECUTING... ]" else "EXECUTE_ALL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (isExecutePressed) Meow.TextHi else Meow.Ink
                )
            }

            // Shuffle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(Meow.Ink, RectangleShape)
                    .border(1.dp, Meow.Line, RectangleShape)
                    .clickable {
                        if (songs.isNotEmpty()) {
                            vm.player.playQueue(songs, shuffle = true)
                            onOpenPlayer()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RANDOMIZE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Meow.TextHi
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- SCROLLABLE CONTENT ---
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // SONGS TAB
                    item {
                        SectionHeader("// RECENT_EXECUTIONS")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(songs.take(5), key = { _, s -> s.id }) { index, song ->
                                RecentlyPlayedCard(index, song) {
                                    vm.player.playQueue(songs, startIndex = index)
                                    onOpenPlayer()
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "// DATABASE [${songs.size}]".uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                color = Meow.TextLo
                            )
                            Icon(Icons.Rounded.Sort, "Sort", tint = Meow.TextHi, modifier = Modifier.size(20.dp))
                        }
                    }

                    itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                        SongListItem(
                            index = index,
                            song = song,
                            isPlaying = ui.songId == song.id && ui.isPlaying,
                            isActive = ui.songId == song.id,
                            cursorAlpha = cursorAlpha
                        ) {
                            vm.player.playQueue(songs, startIndex = index)
                            onOpenPlayer()
                        }
                    }
                }
                1 -> {
                    items(albums, key = { it.albumId }) { album ->
                        AlbumListItem(album) { onNavigate(Route.Album(album.albumId, album.album)) }
                    }
                }
                2 -> {
                    items(artists, key = { it.first }) { (artistName, artistSongs) ->
                        // Passed the first song of the artist to get its album art
                        ArtistListItem(
                            artistName = artistName,
                            songCount = artistSongs.size,
                            representativeSong = artistSongs.first()
                        ) {
                            onNavigate(Route.Artist(artistName))
                        }
                    }
                }
                3 -> {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistListItem(playlist.name, playlist.songIds.size) { onNavigate(Route.PlaylistRoute(playlist.id)) }
                    }
                }
                4 -> {
                    val favSongs = songs.filter { it.id in favs }
                    itemsIndexed(favSongs, key = { _, s -> s.id }) { index, song ->
                        SongListItem(
                            index = index,
                            song = song,
                            isPlaying = ui.songId == song.id && ui.isPlaying,
                            isActive = ui.songId == song.id,
                            cursorAlpha = cursorAlpha
                        ) {
                            vm.player.playQueue(favSongs, startIndex = index)
                            onOpenPlayer()
                        }
                    }
                }
            }
        }

        // --- BOTTOM SYSTEM TASKBAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Meow.PanelHigh)
                .border(1.dp, Meow.Line, RectangleShape)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fake Load Indicator
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYS:",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Meow.TextLo
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(6.dp)
                        .background(Meow.Ink, RectangleShape)
                        .border(1.dp, Meow.Line, RectangleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(loadBarWidth)
                            .fillMaxHeight()
                            .background(Meow.TextHi)
                    )
                }
            }

            Text(
                text = "TRACKS: ${songs.size} | MODE: LOCAL",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        ),
        color = Meow.TextLo,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun RecentlyPlayedCard(index: Int, song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(140.dp).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Meow.PanelHigh).border(1.dp, Meow.Line, RectangleShape).padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ID:${String.format("%02d", index + 1)}",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo
            )
        }

        Box(Modifier.border(1.dp, Meow.Line, RectangleShape)) {
            ArtworkImage(
                song = song,
                modifier = Modifier.size(140.dp),
                shape = RectangleShape,
                px = 256
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = song.title.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = Meow.TextHi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Meow.TextLo,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongListItem(
    index: Int,
    song: Song,
    isActive: Boolean,
    isPlaying: Boolean,
    cursorAlpha: Float,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isActive) Meow.PanelHigh.copy(alpha = 0.3f) else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%02d", index + 1),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = if (isActive) Meow.TextHi else Meow.TextLo,
                modifier = Modifier.width(28.dp)
            )

            Box(Modifier.border(1.dp, if (isActive) Meow.TextHi else Meow.Line, RectangleShape)) {
                ArtworkImage(
                    song = song,
                    modifier = Modifier.size(48.dp),
                    shape = RectangleShape,
                    px = 128
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isActive) Meow.TextHi else Meow.TextHi.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isActive) {
                        Text(
                            text = "_",
                            modifier = Modifier.alpha(cursorAlpha),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Meow.TextHi
                        )
                    }
                }
                Text(
                    text = song.artist.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Meow.TextLo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isPlaying) {
                Icon(Icons.Rounded.GraphicEq, "Playing", tint = Meow.TextHi, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = formatTime(song.durationMs),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Meow.TextLo
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = ">",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo
            )
        }
        HorizontalDivider(color = Meow.Line.copy(alpha = 0.15f), thickness = 1.dp)
    }
}

@Composable
fun AlbumListItem(album: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.border(1.dp, Meow.Line, RectangleShape)) {
            ArtworkImage(
                song = album,
                modifier = Modifier.size(56.dp),
                shape = RectangleShape,
                px = 256
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = album.album.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                ),
                color = Meow.TextHi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ARTIST: ${album.artist.uppercase(Locale.getDefault())}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(">", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace), color = Meow.TextLo)
    }
    HorizontalDivider(color = Meow.Line.copy(alpha = 0.15f), thickness = 1.dp)
}

@Composable
fun ArtistListItem(artistName: String, songCount: Int, representativeSong: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // CHANGED: Now using ArtworkImage instead of the Person Icon
        Box(Modifier.border(1.dp, Meow.Line, RectangleShape)) {
            ArtworkImage(
                song = representativeSong,
                modifier = Modifier.size(56.dp),
                shape = RectangleShape,
                px = 256
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = artistName.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                ),
                color = Meow.TextHi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ENTRIES: $songCount",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo
            )
        }
        Text(">", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace), color = Meow.TextLo)
    }
    HorizontalDivider(color = Meow.Line.copy(alpha = 0.15f), thickness = 1.dp)
}

@Composable
fun PlaylistListItem(playlistName: String, songCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(Meow.PanelHigh, RectangleShape).border(1.dp, Meow.Line, RectangleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.QueueMusic, null, tint = Meow.TextHi)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = playlistName.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                ),
                color = Meow.TextHi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ENTRIES: $songCount",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextLo
            )
        }
        Text(">", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace), color = Meow.TextLo)
    }
    HorizontalDivider(color = Meow.Line.copy(alpha = 0.15f), thickness = 1.dp)
}
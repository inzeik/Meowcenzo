package com.example.meowcenzo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.meowcenzo.MusicViewModel
import com.example.meowcenzo.data.Song
import com.example.meowcenzo.data.SortOrder
import com.example.meowcenzo.playback.PlayerUi
import com.example.meowcenzo.ui.Route
import com.example.meowcenzo.ui.components.ArtworkImage
import com.example.meowcenzo.ui.components.BigTileButton
import com.example.meowcenzo.ui.components.RectButton
import com.example.meowcenzo.ui.components.SongRow
import com.example.meowcenzo.ui.theme.Meow

private enum class HomeTab(val label: String) {
    Songs("Songs"), Albums("Albums"), Artists("Artists"),
    Playlists("Playlists"), Favorites("Favourites"), Folders("Folders"),
}

@Composable
fun HomeScreen(
    vm: MusicViewModel,
    bottomPad: Dp,
    onNavigate: (Route) -> Unit,
    onSongMore: (Song) -> Unit,
    onNewPlaylist: () -> Unit,
) {
    val allSongs by vm.songs.collectAsState()
    val loading by vm.loading.collectAsState()
    val query by vm.query.collectAsState()
    var tab by rememberSaveable { mutableStateOf(HomeTab.Songs) }
    var searching by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Header(
            searching = searching,
            query = query,
            onQuery = { vm.query.value = it },
            onToggleSearch = {
                searching = !searching
                if (!searching) vm.query.value = ""
            },
            onRefresh = vm::scan,
            onEqualizer = { onNavigate(Route.Equalizer) },
        )
        TabStrip(tab) { tab = it }
        Spacer(Modifier.height(4.dp))

        if (allSongs.isEmpty()) {
            EmptyLibrary(loading, onScan = vm::scan)
        } else when (tab) {
            HomeTab.Songs -> SongsTab(vm, bottomPad, onNavigate, onSongMore)
            HomeTab.Albums -> AlbumsTab(vm, bottomPad, onNavigate)
            HomeTab.Artists -> ArtistsTab(vm, bottomPad, onNavigate)
            HomeTab.Playlists -> PlaylistsTab(vm, bottomPad, onNavigate, onNewPlaylist)
            HomeTab.Favorites -> FavoritesTab(vm, bottomPad, onSongMore)
            HomeTab.Folders -> FoldersTab(vm, bottomPad, onNavigate)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Header + tabs
// ---------------------------------------------------------------------------------------------

@Composable
private fun Header(
    searching: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onRefresh: () -> Unit,
    onEqualizer: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (searching) {
            Row(
                Modifier.weight(1f).height(48.dp).clip(Meow.Tile).background(Meow.PanelHigh).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Search, null, tint = Meow.TextLo, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Songs, albums, artists", color = Meow.TextLo, style = MaterialTheme.typography.bodyMedium)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQuery,
                        singleLine = true,
                        textStyle = TextStyle(color = Meow.TextHi, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                        cursorBrush = SolidColor(Meow.Coral),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            RectButton(Icons.Rounded.Close, onToggleSearch, filled = true, description = "Close search")
        } else {
            Text(
                "Meowcenzo",
                style = MaterialTheme.typography.headlineMedium.copy(brush = Meow.gradient),
                modifier = Modifier.weight(1f),
            )
            RectButton(Icons.Rounded.Search, onToggleSearch, description = "Search")
            RectButton(Icons.Rounded.Equalizer, onEqualizer, description = "Equalizer")
            RectButton(Icons.Rounded.Refresh, onRefresh, description = "Rescan library")
        }
    }
}

@Composable
private fun TabStrip(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(HomeTab.entries.toList()) { t ->
            val on = t == selected
            Box(
                Modifier
                    .height(38.dp)
                    .clip(Meow.Tile)
                    .background(if (on) Meow.gradient else Brush.linearGradient(listOf(Meow.Panel, Meow.Panel)))
                    .clickable { onSelect(t) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    t.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) Meow.Ink else Meow.TextLo,
                )
            }
        }
    }
}

@Composable
private fun EmptyLibrary(loading: Boolean, onScan: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(88.dp).clip(Meow.TileLarge).background(Meow.PanelHigh),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.LibraryMusic, null, tint = Meow.Coral, modifier = Modifier.size(40.dp)) }
        Spacer(Modifier.height(20.dp))
        Text(
            if (loading) "Scanning your music" else "No music found",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (loading) "This only takes a moment." else "Copy songs to this phone, then scan again.",
            style = MaterialTheme.typography.bodyMedium, color = Meow.TextLo,
        )
        if (!loading) {
            Spacer(Modifier.height(20.dp))
            BigTileButton("Scan library", Icons.Rounded.Refresh, onScan, Modifier.width(200.dp), primary = true)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tabs
// ---------------------------------------------------------------------------------------------

@Composable
private fun SongsTab(vm: MusicViewModel, bottomPad: Dp, onNavigate: (Route) -> Unit, onSongMore: (Song) -> Unit) {
    val songs by vm.visibleSongs.collectAsState()
    val ui by vm.player.ui.collectAsState()
    val favs by vm.favorites.collectAsState()
    val recents by vm.recentSongs.collectAsState()
    val mostPlayed by vm.mostPlayed.collectAsState()
    val query by vm.query.collectAsState()
    val sort by vm.sort.collectAsState()
    var sortMenu by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(bottom = bottomPad)) {
        item(key = "actions") {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BigTileButton("Play all", Icons.Rounded.PlayArrow, { vm.play(songs, 0) }, Modifier.weight(1f), primary = true)
                BigTileButton("Shuffle", Icons.Rounded.Shuffle, { vm.shuffle(songs) }, Modifier.weight(1f))
            }
        }
        if (query.isBlank() && recents.isNotEmpty()) {
            item(key = "recents") {
                SectionHeader("Recently played")
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recents.take(12), key = { it.id }) { s ->
                        Column(Modifier.width(120.dp).clickable { vm.play(recents, recents.indexOf(s)) }) {
                            ArtworkImage(s, Modifier.size(120.dp), Meow.Tile, px = 300)
                            Spacer(Modifier.height(6.dp))
                            Text(s.title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(s.artist, style = MaterialTheme.typography.bodySmall, color = Meow.TextLo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        if (query.isBlank() && mostPlayed.size >= 3) {
            item(key = "mostplayed") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).clip(Meow.Tile)
                        .background(Meow.Panel).clickable { onNavigate(Route.MostPlayed) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(44.dp).clip(Meow.TileSmall).background(Meow.gradient),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Rounded.PlayArrow, null, tint = Meow.Ink) }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text("Most played", style = MaterialTheme.typography.titleSmall)
                        Text("Your top ${mostPlayed.size} tracks", style = MaterialTheme.typography.bodySmall, color = Meow.TextLo)
                    }
                }
            }
        }
        item(key = "sortbar") {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${songs.size} songs", style = MaterialTheme.typography.labelMedium, color = Meow.TextLo, modifier = Modifier.weight(1f))
                Box {
                    RectButton(Icons.AutoMirrored.Rounded.Sort, { sortMenu = true }, tint = Meow.TextLo, description = "Sort")
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }, containerColor = Meow.PanelHigh) {
                        SortOrder.entries.forEach { o ->
                            DropdownMenuItem(
                                text = { Text(o.label, color = if (o == sort) Meow.Coral else Meow.TextHi) },
                                onClick = { vm.sort.value = o; sortMenu = false },
                            )
                        }
                    }
                }
            }
        }
        itemsIndexed(songs, key = { _, s -> s.id }) { i, s ->
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

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 10.dp),
    )
}

@Composable
private fun AlbumsTab(vm: MusicViewModel, bottomPad: Dp, onNavigate: (Route) -> Unit) {
    val albums by vm.albums.collectAsState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPad),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        gridItems(albums, key = { "${it.id}:${it.name}" }) { a ->
            Column(Modifier.clickable { onNavigate(Route.Album(a.id, a.name)) }) {
                ArtworkImage(a.songs.first(), Modifier.fillMaxWidth().aspectRatio(1f), Meow.Tile, px = 400)
                Spacer(Modifier.height(8.dp))
                Text(a.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(a.artist, style = MaterialTheme.typography.bodySmall, color = Meow.TextLo, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ArtistsTab(vm: MusicViewModel, bottomPad: Dp, onNavigate: (Route) -> Unit) {
    val artists by vm.artists.collectAsState()
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPad)) {
        items(artists, key = { it.name }) { a ->
            Row(
                Modifier.fillMaxWidth().clickable { onNavigate(Route.Artist(a.name)) }.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArtworkImage(a.songs.first(), Modifier.size(56.dp), Meow.Tile, px = 160)
                Column(Modifier.padding(start = 14.dp)) {
                    Text(a.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${a.songs.size} songs, ${a.albumCount} albums",
                        style = MaterialTheme.typography.bodySmall, color = Meow.TextLo,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistsTab(vm: MusicViewModel, bottomPad: Dp, onNavigate: (Route) -> Unit, onNew: () -> Unit) {
    val playlists by vm.playlists.collectAsState()
    val songMap by vm.songMap.collectAsState()
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPad)) {
        item(key = "new") {
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onNew).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(56.dp).clip(Meow.Tile).background(Meow.PanelHigh),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Add, null, tint = Meow.Coral) }
                Text("New playlist", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 14.dp))
            }
        }
        items(playlists, key = { it.id }) { p ->
            val first = p.songIds.firstNotNullOfOrNull { songMap[it] }
            Row(
                Modifier.fillMaxWidth().clickable { onNavigate(Route.PlaylistRoute(p.id)) }.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (first != null) ArtworkImage(first, Modifier.size(56.dp), Meow.Tile, px = 160)
                else Box(
                    Modifier.size(56.dp).clip(Meow.Tile).background(Meow.PanelHigh),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.AutoMirrored.Rounded.QueueMusic, null, tint = Meow.TextLo) }
                Column(Modifier.padding(start = 14.dp)) {
                    Text(p.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${p.songIds.size} songs", style = MaterialTheme.typography.bodySmall, color = Meow.TextLo)
                }
            }
        }
    }
}

@Composable
private fun FavoritesTab(vm: MusicViewModel, bottomPad: Dp, onSongMore: (Song) -> Unit) {
    val songs by vm.favoriteSongs.collectAsState()
    val ui: PlayerUi by vm.player.ui.collectAsState()
    if (songs.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Tap the heart on Now Playing to keep a song here.", color = Meow.TextLo, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = bottomPad)) {
        itemsIndexed(songs, key = { _, s -> s.id }) { i, s ->
            SongRow(s, ui.songId == s.id, ui.isPlaying, true, { vm.play(songs, i) }, { onSongMore(s) })
        }
    }
}

@Composable
private fun FoldersTab(vm: MusicViewModel, bottomPad: Dp, onNavigate: (Route) -> Unit) {
    val folders by vm.folders.collectAsState()
    LazyColumn(contentPadding = PaddingValues(bottom = bottomPad)) {
        items(folders, key = { it.path }) { f ->
            Row(
                Modifier.fillMaxWidth().clickable { onNavigate(Route.FolderRoute(f.path)) }.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(52.dp).clip(Meow.Tile).background(Meow.PanelHigh),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Folder, null, tint = Meow.Lilac) }
                Column(Modifier.padding(start = 14.dp)) {
                    Text(f.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${f.songs.size} songs",
                        style = MaterialTheme.typography.bodySmall, color = Meow.TextLo,
                    )
                }
            }
        }
    }
}

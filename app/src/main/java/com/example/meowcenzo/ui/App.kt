package com.example.meowcenzo.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meowcenzo.MusicViewModel
import com.example.meowcenzo.data.Song
import com.example.meowcenzo.ui.components.BigTileButton
import com.example.meowcenzo.ui.components.NamePlaylistDialog
import com.example.meowcenzo.ui.components.PlaylistPickerDialog
import com.example.meowcenzo.ui.components.SongOptionsSheet
import com.example.meowcenzo.ui.screens.DetailScreen
import com.example.meowcenzo.ui.screens.EqualizerScreen
import com.example.meowcenzo.ui.screens.HomeScreen
import com.example.meowcenzo.ui.screens.MiniPlayer
import com.example.meowcenzo.ui.screens.NowPlayingScreen
import com.example.meowcenzo.ui.theme.Meow

@Composable
fun MeowcenzoApp(vm: MusicViewModel = viewModel()) {
    val context = LocalContext.current
    val audioPerm = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val toAsk = remember {
        buildList {
            add(audioPerm)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
    }
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, audioPerm) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = result[audioPerm] == true
    }
    LaunchedEffect(granted) { if (granted) vm.scan() }

    Surface(Modifier.fillMaxSize(), color = Meow.Ink, contentColor = Meow.TextHi) {
        if (granted) Shell(vm) else PermissionScreen(onAllow = { launcher.launch(toAsk) })
    }
}

@Composable
private fun Shell(vm: MusicViewModel) {
    val ui by vm.player.ui.collectAsState()
    val songs by vm.songs.collectAsState()
    val songMap by vm.songMap.collectAsState()
    val favs by vm.favorites.collectAsState()
    val playlists by vm.playlists.collectAsState()
    val mostPlayed by vm.mostPlayed.collectAsState()

    var stack by remember { mutableStateOf(listOf<Route>()) }
    var showNow by remember { mutableStateOf(false) }
    var optionsSong by remember { mutableStateOf<Song?>(null) }
    var optionsPlaylistId by remember { mutableStateOf<String?>(null) }
    var pickerSong by remember { mutableStateOf<Song?>(null) }
    var pendingSong by remember { mutableStateOf<Song?>(null) }
    var showNewPlaylist by remember { mutableStateOf(false) }

    val miniVisible = ui.songId != null
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomPad = navBottom + if (miniVisible) 96.dp else 16.dp

    BackHandler(enabled = showNow) { showNow = false }
    BackHandler(enabled = !showNow && stack.isNotEmpty()) { stack = stack.dropLast(1) }

    val push: (Route) -> Unit = { stack = stack + it }
    val pop: () -> Unit = { stack = stack.dropLast(1) }
    val openOptions: (Song) -> Unit = {
        optionsSong = it
        optionsPlaylistId = (stack.lastOrNull() as? Route.PlaylistRoute)?.id
    }

    Box(Modifier.fillMaxSize().background(Meow.Ink)) {
        Box(Modifier.fillMaxSize().statusBarsPadding()) {
            when (val route = stack.lastOrNull()) {
                null -> HomeScreen(
                    vm = vm,
                    bottomPad = bottomPad,
                    onNavigate = push,
                    onSongMore = openOptions,
                    onNewPlaylist = { pendingSong = null; showNewPlaylist = true },
                )
                is Route.Album -> {
                    val list = songs.filter { it.albumId == route.id && it.album == route.name }.sortedBy { it.track }
                    DetailScreen(vm, route.name, list.firstOrNull()?.artist.orEmpty(), list, bottomPad, pop, openOptions)
                }
                is Route.Artist -> {
                    val list = songs.filter { it.artist == route.name }.sortedBy { it.album.lowercase() }
                    DetailScreen(vm, route.name, "${list.size} songs", list, bottomPad, pop, openOptions)
                }
                is Route.FolderRoute -> {
                    val list = songs.filter { it.folderPath == route.path }.sortedBy { it.title.lowercase() }
                    DetailScreen(vm, list.firstOrNull()?.folderName ?: "Folder", route.path, list, bottomPad, pop, openOptions)
                }
                is Route.PlaylistRoute -> {
                    val p = playlists.find { it.id == route.id }
                    if (p == null) {
                        LaunchedEffect(Unit) { pop() }
                    } else {
                        val list = p.songIds.mapNotNull { songMap[it] }
                        DetailScreen(
                            vm, p.name, "${list.size} songs", list, bottomPad, pop, openOptions,
                            onDeleteList = { vm.deletePlaylist(p.id); pop() },
                        )
                    }
                }
                Route.MostPlayed ->
                    DetailScreen(vm, "Most played", "${mostPlayed.size} songs", mostPlayed, bottomPad, pop, openOptions)
                Route.Equalizer -> EqualizerScreen(onBack = pop, bottomPad = bottomPad)
            }
        }

        if (miniVisible && !showNow) {
            MiniPlayer(
                vm = vm,
                onOpen = { showNow = true },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp),
            )
        }

        AnimatedVisibility(
            visible = showNow,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
        ) {
            NowPlayingScreen(
                vm = vm,
                onClose = { showNow = false },
                onOpenEqualizer = { showNow = false; push(Route.Equalizer) },
            )
        }
    }

    optionsSong?.let { s ->
        SongOptionsSheet(
            song = s,
            isFavorite = s.id in favs,
            onDismiss = { optionsSong = null },
            onPlayNext = { vm.player.playNext(s) },
            onAddToQueue = { vm.player.addToQueue(s) },
            onToggleFavorite = { vm.toggleFavorite(s.id) },
            onAddToPlaylist = { pickerSong = s },
            onRemoveFromPlaylist = optionsPlaylistId?.let { pid -> { vm.removeFromPlaylist(pid, s.id) } },
        )
    }

    pickerSong?.let { s ->
        PlaylistPickerDialog(
            playlists = playlists,
            onPick = { vm.addToPlaylist(it.id, s.id) },
            onNew = { pendingSong = s; showNewPlaylist = true },
            onDismiss = { pickerSong = null },
        )
    }

    if (showNewPlaylist) {
        NamePlaylistDialog(
            onDismiss = { showNewPlaylist = false; pendingSong = null },
            onCreate = { vm.createPlaylist(it, pendingSong?.id) },
        )
    }
}

@Composable
private fun PermissionScreen(onAllow: () -> Unit) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(
            Modifier.size(104.dp).clip(Meow.TileLarge).background(Meow.gradient),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.MusicNote, null, tint = Meow.Ink, modifier = Modifier.size(52.dp)) }
        Spacer(Modifier.height(24.dp))
        Text("Your music, offline", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Meowcenzo plays the songs already on this phone. Allow access to your audio files to build your library.",
            style = MaterialTheme.typography.bodyMedium, color = Meow.TextLo, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        BigTileButton("Allow access", Icons.Rounded.LibraryMusic, onAllow, Modifier.fillMaxWidth(), primary = true)
        TextButton(onClick = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            )
        }) { Text("Open app settings", color = Meow.TextLo) }
    }
}

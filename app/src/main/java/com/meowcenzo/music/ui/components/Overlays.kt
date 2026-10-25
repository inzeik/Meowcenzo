package com.meowcenzo.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.meowcenzo.music.data.Playlist
import com.meowcenzo.music.data.Song
import com.meowcenzo.music.ui.theme.Meow

@Composable
private fun SheetAction(icon: ImageVector, label: String, tint: Color = Meow.TextHi, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(18.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Meow.TextHi)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    song: Song,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Meow.Panel,
        contentColor = Meow.TextHi,
        shape = Meow.TileLarge,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkImage(song, Modifier.size(56.dp), Meow.Tile, px = 160)
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = Meow.TextLo, maxLines = 1)
            }
        }
        Spacer(Modifier.height(8.dp))
        SheetAction(Icons.AutoMirrored.Rounded.PlaylistPlay, "Play next") { onPlayNext(); onDismiss() }
        SheetAction(Icons.AutoMirrored.Rounded.QueueMusic, "Add to queue") { onAddToQueue(); onDismiss() }
        SheetAction(
            if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            if (isFavorite) "Remove from favourites" else "Add to favourites",
            tint = Meow.Coral,
        ) { onToggleFavorite(); onDismiss() }
        SheetAction(Icons.AutoMirrored.Rounded.PlaylistAdd, "Add to playlist") { onAddToPlaylist(); onDismiss() }
        if (onRemoveFromPlaylist != null) {
            SheetAction(Icons.Rounded.Delete, "Remove from this playlist", Meow.Coral) { onRemoveFromPlaylist(); onDismiss() }
        }
        Spacer(Modifier.height(24.dp).navigationBarsPadding())
    }
}

@Composable
fun NamePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Meow.Panel,
        shape = Meow.TileLarge,
        titleContentColor = Meow.TextHi,
        title = { Text("New playlist") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Playlist name") },
                shape = Meow.Tile,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Meow.PanelHigh,
                    unfocusedContainerColor = Meow.PanelHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Meow.Coral,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name); onDismiss() }, enabled = name.isNotBlank()) {
                Text("Create", color = if (name.isNotBlank()) Meow.Coral else Meow.TextLo)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Meow.TextLo) } },
    )
}

@Composable
fun PlaylistPickerDialog(
    playlists: List<Playlist>,
    onPick: (Playlist) -> Unit,
    onNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Meow.Panel,
        shape = Meow.TileLarge,
        titleContentColor = Meow.TextHi,
        title = { Text("Add to playlist") },
        text = {
            LazyColumn {
                item {
                    SheetAction(Icons.Rounded.Add, "New playlist", Meow.Coral) { onNew(); onDismiss() }
                }
                items(playlists, key = { it.id }) { p ->
                    SheetAction(Icons.AutoMirrored.Rounded.QueueMusic, p.name) { onPick(p); onDismiss() }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Meow.TextLo) } },
    )
}

@Composable
fun SleepTimerDialog(activeMinutesLabel: String?, onPick: (Int) -> Unit, onCancelTimer: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Meow.Panel,
        shape = Meow.TileLarge,
        titleContentColor = Meow.TextHi,
        title = { Text("Sleep timer") },
        text = {
            Column {
                listOf(5, 10, 15, 30, 45, 60).forEach { m ->
                    Text(
                        "$m minutes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Meow.TextHi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(m); onDismiss() }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
        confirmButton = {
            if (activeMinutesLabel != null) {
                TextButton(onClick = { onCancelTimer(); onDismiss() }) {
                    Text("Turn off ($activeMinutesLabel left)", color = Meow.Coral)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Meow.TextLo) } },
    )
}

package com.example.meowcenzo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.meowcenzo.MusicViewModel
import com.example.meowcenzo.ui.components.ArtworkImage
import com.example.meowcenzo.ui.components.RectButton
import com.example.meowcenzo.ui.theme.Meow

@Composable
fun MiniPlayer(vm: MusicViewModel, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val ui by vm.player.ui.collectAsState()
    val songMap by vm.songMap.collectAsState()
    if (ui.songId == null) return
    val song = songMap[ui.songId]
    val progress = if (ui.durationMs > 0) (ui.positionMs.toFloat() / ui.durationMs).coerceIn(0f, 1f) else 0f

    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(Meow.Tile)
            .background(Meow.PanelHigh)
            .clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ArtworkImage(song, Modifier.size(46.dp), Meow.TileSmall, px = 128)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    song?.title ?: ui.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(
                    song?.artist ?: ui.artist,
                    style = MaterialTheme.typography.bodySmall, color = Meow.TextLo,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            RectButton(
                if (ui.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                onClick = { vm.player.togglePlay() },
                filled = true, tint = Meow.Coral, description = "Play or pause",
            )
            RectButton(Icons.Rounded.SkipNext, onClick = { vm.player.next() }, description = "Next")
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(Meow.Line)) {
            Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Meow.gradient))
        }
    }
}

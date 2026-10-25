package com.meowcenzo.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape // <--- FIXED IMPORT
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meowcenzo.music.MusicViewModel
import com.meowcenzo.music.ui.components.ArtworkImage
import com.meowcenzo.music.ui.theme.Meow
import java.util.Locale

@Composable
fun MiniPlayer(vm: MusicViewModel, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val ui by vm.player.ui.collectAsState()
    val songMap by vm.songMap.collectAsState()

    if (ui.songId == null) return
    val song = songMap[ui.songId]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Meow.Ink)
            .border(1.dp, Meow.Line, RectangleShape)
            .clickable(onClick = onOpen)
    ) {
        // SEGMENTED PROGRESS BAR (Top Edge)
        Row(modifier = Modifier.fillMaxWidth().height(2.dp).background(Meow.PanelHigh)) {
            val progress = if (ui.durationMs > 0) (ui.positionMs.toFloat() / ui.durationMs).coerceIn(0f, 1f) else 0f
            Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(Meow.TextHi))
        }

        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtworkImage(
                song = song,
                modifier = Modifier.size(48.dp).border(1.dp, Meow.Line, RectangleShape),
                shape = RectangleShape,
                px = 128
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = (song?.title ?: ui.title).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = Meow.TextHi,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (song?.artist ?: ui.artist).uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Meow.TextLo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { vm.player.togglePlay() }) {
                Icon(
                    if (ui.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Meow.TextHi
                )
            }
            IconButton(onClick = { vm.player.next() }) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = Meow.TextHi
                )
            }
        }
    }
}
package com.meowcenzo.music.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.meowcenzo.music.MusicViewModel
import com.meowcenzo.music.playback.SleepTimer
import com.meowcenzo.music.ui.components.ArtworkCache
import com.meowcenzo.music.ui.components.ArtworkImage
import com.meowcenzo.music.ui.components.GradientPlayButton
import com.meowcenzo.music.ui.components.RectButton
import com.meowcenzo.music.ui.components.SleepTimerDialog
import com.meowcenzo.music.ui.components.WaveformSeekBar
import com.meowcenzo.music.ui.components.accentColor
import com.meowcenzo.music.ui.components.formatTime
import com.meowcenzo.music.ui.theme.Meow

@Composable
fun NowPlayingScreen(vm: MusicViewModel, onClose: () -> Unit, onOpenEqualizer: () -> Unit) {
    val ui by vm.player.ui.collectAsState()
    val songMap by vm.songMap.collectAsState()
    val favs by vm.favorites.collectAsState()
    val remaining by SleepTimer.remainingMs.collectAsState()
    val context = LocalContext.current
    val song = ui.songId?.let { songMap[it] }

    var scrub by remember { mutableStateOf<Float?>(null) }
    var showQueue by remember { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }

    val progress = scrub ?: if (ui.durationMs > 0) (ui.positionMs.toFloat() / ui.durationMs).coerceIn(0f, 1f) else 0f

    // Background tint is pulled from the album art (Apple Music style).
    val accent by produceState(Meow.PanelHigh, song?.artKey) {
        val bmp = song?.let { ArtworkCache.load(context, it, 128) }
        value = bmp?.accentColor() ?: Meow.PanelHigh
    }
    val tint by animateColorAsState(accent, tween(700), label = "tint")
    val artScale by animateFloatAsState(
        if (ui.isPlaying) 1f else 0.88f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "artScale",
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(0f to tint, 0.85f to Meow.Ink))
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
        ) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                RectButton(Icons.Rounded.KeyboardArrowDown, onClose, filled = true, description = "Close")
                Text(
                    "Now playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = Meow.TextHi.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                RectButton(Icons.AutoMirrored.Rounded.QueueMusic, { showQueue = true }, filled = true, description = "Queue")
            }

            // Artwork
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                val side = minOf(maxWidth, maxHeight)
                ArtworkImage(
                    song,
                    Modifier.size(side).graphicsLayer { scaleX = artScale; scaleY = artScale },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    px = 900,
                )
            }

            // Title + favourite
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        song?.title ?: ui.title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        song?.artist ?: ui.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Meow.TextHi.copy(alpha = 0.7f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                val isFav = ui.songId?.let { it in favs } == true
                RectButton(
                    if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    onClick = { ui.songId?.let(vm::toggleFavorite) },
                    filled = true, tint = if (isFav) Meow.Coral else Meow.TextHi,
                    description = "Favourite",
                )
            }

            Spacer(Modifier.height(18.dp))

            WaveformSeekBar(
                seed = ui.songId ?: 0L,
                progress = progress,
                onScrub = { scrub = it },
                onScrubEnd = { vm.player.seekToFraction(it); scrub = null },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            )
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                val shown = (progress * ui.durationMs).toLong()
                Text(formatTime(shown), style = MaterialTheme.typography.labelMedium, color = Meow.TextHi.copy(alpha = 0.7f))
                Spacer(Modifier.weight(1f))
                Text(formatTime(ui.durationMs), style = MaterialTheme.typography.labelMedium, color = Meow.TextHi.copy(alpha = 0.7f))
            }

            Spacer(Modifier.height(18.dp))

            // Transport
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RectButton(
                    Icons.Rounded.Shuffle, { vm.player.toggleShuffle() },
                    tint = if (ui.shuffle) Meow.Coral else Meow.TextHi.copy(alpha = 0.7f), description = "Shuffle",
                )
                RectButton(Icons.Rounded.SkipPrevious, { vm.player.previous() }, size = 56.dp, iconSize = 32.dp, filled = true, description = "Previous")
                GradientPlayButton(ui.isPlaying, { vm.player.togglePlay() })
                RectButton(Icons.Rounded.SkipNext, { vm.player.next() }, size = 56.dp, iconSize = 32.dp, filled = true, description = "Next")
                RectButton(
                    if (ui.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    { vm.player.cycleRepeat() },
                    tint = if (ui.repeatMode != Player.REPEAT_MODE_OFF) Meow.Coral else Meow.TextHi.copy(alpha = 0.7f),
                    description = "Repeat",
                )
            }

            Spacer(Modifier.height(20.dp))

            // Utility tiles
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val speeds = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
                UtilityTile(Icons.Rounded.Speed, "${ui.speed}x", Modifier.weight(1f)) {
                    val next = speeds[(speeds.indexOfFirst { it == ui.speed } + 1) % speeds.size]
                    vm.player.setSpeed(next)
                }
                UtilityTile(
                    Icons.Rounded.Bedtime,
                    if (remaining > 0) formatTime(remaining) else "Sleep",
                    Modifier.weight(1f),
                    active = remaining > 0,
                ) { showSleep = true }
                UtilityTile(Icons.Rounded.Equalizer, "EQ", Modifier.weight(1f), onClick = onOpenEqualizer)
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showQueue) QueueSheet(vm, onDismiss = { showQueue = false })
    if (showSleep) {
        SleepTimerDialog(
            activeMinutesLabel = if (remaining > 0) formatTime(remaining) else null,
            onPick = { SleepTimer.start(it) },
            onCancelTimer = { SleepTimer.cancel() },
            onDismiss = { showSleep = false },
        )
    }
}

@Composable
private fun UtilityTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .height(46.dp)
            .clip(Meow.Tile)
            .background(Color.Black.copy(alpha = 0.28f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (active) Meow.Coral else Meow.TextHi, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = if (active) Meow.Coral else Meow.TextHi)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(vm: MusicViewModel, onDismiss: () -> Unit) {
    val ui by vm.player.ui.collectAsState()
    val songMap by vm.songMap.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Meow.Panel,
        contentColor = Meow.TextHi,
        shape = Meow.TileLarge,
    ) {
        Text(
            "Up next, ${ui.queueIds.size} songs",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        LazyColumn(Modifier.heightIn(max = 520.dp)) {
            itemsIndexed(ui.queueIds) { i, id ->
                val s = songMap[id]
                val current = i == ui.index
                Row(
                    Modifier.fillMaxWidth().clickable { vm.player.skipTo(i) }.padding(start = 24.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ArtworkImage(s, Modifier.size(44.dp), Meow.TileSmall, px = 128)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(
                            s?.title ?: "Unknown",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (current) Meow.Coral else Meow.TextHi,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Text(s?.artist ?: "", style = MaterialTheme.typography.bodySmall, color = Meow.TextLo, maxLines = 1)
                    }
                    if (!current) {
                        RectButton(Icons.Rounded.Close, { vm.player.removeAt(i) }, tint = Meow.TextLo, description = "Remove")
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp).navigationBarsPadding())
    }
}

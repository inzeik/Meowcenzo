package com.meowcenzo.music.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meowcenzo.music.data.Song
import com.meowcenzo.music.ui.theme.Meow

fun formatTime(ms: Long): String {
    val total = (ms / 1000L).coerceAtLeast(0L)
    return "%d:%02d".format(total / 60, total % 60)
}

/** The app's signature control: a rounded-rectangle tile instead of a circular icon button. */
@Composable
fun RectButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    filled: Boolean = false,
    tint: Color = Meow.TextHi,
    description: String? = null,
    shape: Shape = Meow.Tile,
) {
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(if (filled) Meow.PanelHigh else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun GradientPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 92.dp,
    height: Dp = 64.dp,
) {
    Box(
        modifier
            .size(width, height)
            .clip(RoundedCornerShape(20.dp))
            .background(Meow.gradient)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Meow.Ink,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
fun BigTileButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val fg = if (primary) Meow.Ink else Meow.TextHi
    Row(
        modifier
            .height(48.dp)
            .clip(Meow.Tile)
            .background(if (primary) Meow.gradient else Brush.linearGradient(listOf(Meow.PanelHigh, Meow.PanelHigh)))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
    }
}

/** Three square-ended bars that dance next to the track that is playing. */
@Composable
fun PlayingBars(modifier: Modifier = Modifier, animate: Boolean = true, color: Color = Meow.Coral) {
    val t = rememberInfiniteTransition(label = "bars")
    val a by t.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "a"
    )
    val b by t.animateFloat(
        initialValue = 1f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(560, easing = LinearEasing), RepeatMode.Reverse), label = "b"
    )
    val c by t.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse), label = "c"
    )
    Canvas(modifier.size(18.dp)) {
        val w = size.width / 5f
        listOf(a, b, c).forEachIndexed { i, v ->
            val h = size.height * (if (animate) v else 0.4f)
            drawRoundRect(color, Offset(i * 2f * w, size.height - h), Size(w, h), CornerRadius(2f, 2f))
        }
    }
}

/**
 * SoundCloud-style waveform scrubber. Bars are seeded from the track id so each song keeps its own
 * stable shape. Tap or drag anywhere to seek.
 */
@Composable
fun WaveformSeekBar(
    seed: Long,
    progress: Float,
    onScrub: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit,
    modifier: Modifier = Modifier,
    bars: Int = 56,
) {
    val heights = remember(seed, bars) {
        val rnd = java.util.Random(seed * 7919L + 13L)
        val raw = FloatArray(bars) { 0.25f + rnd.nextFloat() * 0.75f }
        FloatArray(bars) { i ->
            val l = raw.getOrElse(i - 1) { raw[i] }
            val r = raw.getOrElse(i + 1) { raw[i] }
            (l + raw[i] * 2f + r) / 4f
        }
    }
    val scrub by rememberUpdatedState(onScrub)
    val scrubEnd by rememberUpdatedState(onScrubEnd)

    Canvas(
        modifier
            .pointerInput(Unit) {
                detectTapGestures { o -> scrubEnd((o.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                var last = 0f
                detectHorizontalDragGestures(
                    onDragStart = { o ->
                        last = (o.x / size.width).coerceIn(0f, 1f)
                        scrub(last)
                    },
                    onDragEnd = { scrubEnd(last) },
                    onDragCancel = { scrubEnd(last) },
                    onHorizontalDrag = { change, _ ->
                        last = (change.position.x / size.width).coerceIn(0f, 1f)
                        scrub(last)
                    },
                )
            }
    ) {
        val barW = size.width / (bars * 1.6f)
        val gap = (size.width - barW * bars) / (bars - 1)
        val brush = Brush.horizontalGradient(listOf(Meow.Coral, Meow.Lilac), startX = 0f, endX = size.width)
        val playedCount = (progress.coerceIn(0f, 1f) * bars)
        for (i in 0 until bars) {
            val h = (heights[i] * size.height).coerceAtLeast(6.dp.toPx())
            val topLeft = Offset(i * (barW + gap), (size.height - h) / 2f)
            val corner = CornerRadius(barW / 3f, barW / 3f)
            if (i < playedCount) drawRoundRect(brush, topLeft, Size(barW, h), corner)
            else drawRoundRect(Color.White.copy(alpha = 0.22f), topLeft, Size(barW, h), corner)
        }
        // Playhead: a slim rectangle.
        val x = progress.coerceIn(0f, 1f) * size.width
        drawRoundRect(
            Color.White,
            Offset((x - 1.5.dp.toPx()).coerceIn(0f, size.width - 3.dp.toPx()), 0f),
            Size(3.dp.toPx(), size.height),
            CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
    }
}

@Composable
fun SongRow(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center) {
            ArtworkImage(song, Modifier.size(52.dp), Meow.TileSmall, px = 160)
            if (isCurrent) {
                Box(
                    Modifier.size(52.dp).clip(Meow.TileSmall).background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) { PlayingBars(animate = isPlaying) }
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isCurrent) Meow.Coral else Meow.TextHi,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Meow.TextLo,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        if (isFavorite) {
            Icon(Icons.Rounded.Favorite, null, tint = Meow.Coral, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(formatTime(song.durationMs), style = MaterialTheme.typography.bodySmall, color = Meow.TextLo)
        RectButton(Icons.Rounded.MoreVert, onClick = onMore, tint = Meow.TextLo, description = "More")
    }
}

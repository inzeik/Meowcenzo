package com.meowcenzo.music.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meowcenzo.music.playback.EqualizerManager
import com.meowcenzo.music.ui.theme.Meow
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    bottomPad: Dp
) {
    // Collect the REAL state from your manager
    val eqState by EqualizerManager.state.collectAsState()

    // If the device doesn't support EQ, show a warning
    if (!eqState.available) {
        Column(
            modifier = Modifier.fillMaxSize().background(Meow.Ink),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "EQUALIZER NOT SUPPORTED",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace),
                color = Meow.TextHi
            )
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.size(40.dp).background(Meow.PanelHigh, RectangleShape).border(1.dp, Meow.Line, RectangleShape).clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Meow.TextHi)
            }
        }
        return
    }

    var selectedPreset by remember { mutableStateOf("CUSTOM") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Meow.Ink)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPad)
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
                Box(
                    Modifier
                        .size(40.dp)
                        .background(Meow.PanelHigh, RectangleShape)
                        .border(1.dp, Meow.Line, RectangleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Meow.TextHi)
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "EQUALIZER".uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    ),
                    color = Meow.TextHi
                )
            }

            Switch(
                checked = eqState.enabled,
                onCheckedChange = { EqualizerManager.toggle(it) }, // Hooks to Manager
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Meow.Ink,
                    checkedTrackColor = Meow.TextHi,
                    uncheckedThumbColor = Meow.TextLo,
                    uncheckedTrackColor = Meow.PanelHigh,
                    uncheckedBorderColor = Meow.Line
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- PRESETS ---
        Text(
            text = "PRESETS",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black),
            color = Meow.TextHi,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Use the REAL presets from Android's system Equalizer
            itemsIndexed(eqState.presets) { index, presetName ->
                val isSelected = selectedPreset == presetName
                Box(
                    modifier = Modifier
                        .background(if (isSelected) Meow.TextHi else Meow.PanelHigh, RectangleShape)
                        .border(1.dp, if (isSelected) Meow.TextHi else Meow.Line, RectangleShape)
                        .clickable {
                            selectedPreset = presetName
                            EqualizerManager.applyPreset(index) // Hooks to Manager
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = presetName.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black),
                        color = if (isSelected) Meow.Ink else Meow.TextLo
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- BANDS ---
        Text(
            text = "BANDS",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black),
            color = Meow.TextHi,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val minLevel = eqState.minLevel.toFloat()
        val maxLevel = eqState.maxLevel.toFloat()
        val levelRange = maxLevel - minLevel

        eqState.bandFreqsHz.forEachIndexed { index, freq ->
            val currentLevel = eqState.levels.getOrElse(index) { 0 }.toFloat()
            // Convert millibels (-1500 to +1500) into a 0.0 to 1.0 value for the slider
            val sliderValue = ((currentLevel - minLevel) / levelRange).coerceIn(0f, 1f)

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val freqText = if (freq >= 1000) "${freq / 1000} KHZ" else "$freq HZ"
                    Text(
                        text = freqText.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        color = Meow.TextHi
                    )

                    // Millibels to Decibels (divide by 100)
                    val dbValue = currentLevel / 100f
                    val dbString = if (dbValue > 0.01f) "+%.1f DB" else "%.1f DB"
                    Text(
                        text = String.format(Locale.US, dbString, dbValue),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = Meow.TextLo
                    )
                }

                BrutalistSlider(
                    value = sliderValue,
                    onValueChange = { newSliderValue ->
                        // Convert back from 0.0-1.0 to millibels and send to Manager
                        val newLevel = (minLevel + (newSliderValue * levelRange)).roundToInt()
                        EqualizerManager.setBandLevel(index, newLevel)
                        selectedPreset = "CUSTOM"
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- BASS BOOST ---
        if (eqState.bassSupported) {
            Text(
                text = "BASS BOOST",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black),
                color = Meow.TextHi,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STRENGTH",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        color = Meow.TextHi
                    )
                    // Bass strength is 0..1000, so we divide by 10 to get 0..100%
                    Text(
                        text = "${eqState.bassStrength / 10}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = Meow.TextLo
                    )
                }

                BrutalistSlider(
                    value = eqState.bassStrength / 1000f,
                    onValueChange = { newValue ->
                        val newStrength = (newValue * 1000).roundToInt()
                        EqualizerManager.setBass(newStrength)
                        selectedPreset = "CUSTOM"
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    isCenterBased = false
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// --- CUSTOM SHARP SLIDER ---
@Composable
fun BrutalistSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isCenterBased: Boolean = true
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val width = size.width.toFloat()
                    if (width > 0) {
                        var fraction = (down.position.x / width).coerceIn(0f, 1f)
                        onValueChange(fraction)
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            fraction = (change.position.x / width).coerceIn(0f, 1f)
                            onValueChange(fraction)
                            change.consume()
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        val thumbWidth = 8.dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val thumbX = w * value

            drawRect(
                color = Meow.PanelHigh,
                topLeft = Offset(0f, h / 2f - 4.dp.toPx()),
                size = Size(w, 8.dp.toPx())
            )

            if (isCenterBased) {
                if (value > 0.5f) {
                    drawRect(color = Meow.TextHi, topLeft = Offset(centerX, h / 2f - 4.dp.toPx()), size = Size(thumbX - centerX, 8.dp.toPx()))
                } else if (value < 0.5f) {
                    drawRect(color = Meow.TextHi, topLeft = Offset(thumbX, h / 2f - 4.dp.toPx()), size = Size(centerX - thumbX, 8.dp.toPx()))
                }
            } else {
                drawRect(color = Meow.TextHi, topLeft = Offset(0f, h / 2f - 4.dp.toPx()), size = Size(thumbX, 8.dp.toPx()))
            }

            val actualThumbX = (thumbX - (thumbWidth.toPx() / 2f)).coerceIn(0f, w - thumbWidth.toPx())
            drawRect(
                color = Meow.TextHi,
                topLeft = Offset(actualThumbX, 0f),
                size = Size(thumbWidth.toPx(), h)
            )
        }
    }
}
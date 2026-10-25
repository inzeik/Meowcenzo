package com.example.meowcenzo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.meowcenzo.playback.EqualizerManager
import com.example.meowcenzo.ui.components.RectButton
import com.example.meowcenzo.ui.theme.Meow
import kotlin.math.roundToInt

@Composable
fun EqualizerScreen(onBack: () -> Unit, bottomPad: Dp) {
    val eq by EqualizerManager.state.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = bottomPad)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RectButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack, filled = true, description = "Back")
            Text("Equalizer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).padding(start = 14.dp))
            Switch(
                checked = eq.enabled,
                onCheckedChange = { EqualizerManager.toggle(it) },
                enabled = eq.available,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Meow.Coral, checkedThumbColor = Meow.Ink,
                    uncheckedTrackColor = Meow.PanelHigh, uncheckedThumbColor = Meow.TextLo,
                    uncheckedBorderColor = Meow.Line,
                ),
            )
            Spacer(Modifier.width(8.dp))
        }

        if (!eq.available) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Start playing a song and the equalizer will unlock.",
                    style = MaterialTheme.typography.bodyMedium, color = Meow.TextLo,
                )
            }
        } else {
        if (eq.presets.isNotEmpty()) {
            Text("Presets", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(eq.presets) { i, name ->
                    Box(
                        Modifier.height(38.dp).clip(Meow.Tile).background(Meow.PanelHigh)
                            .clickable { EqualizerManager.applyPreset(i) }.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text(name, style = MaterialTheme.typography.labelLarge) }
                }
            }
        }

        Text("Bands", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp))
        eq.levels.forEachIndexed { band, level ->
            val hz = eq.bandFreqsHz.getOrElse(band) { 0 }
            BandSlider(
                label = if (hz >= 1000) "${hz / 1000} kHz" else "$hz Hz",
                valueLabel = "%+.1f dB".format(level / 100f),
                value = level.toFloat(),
                range = eq.minLevel.toFloat()..eq.maxLevel.toFloat(),
                enabled = eq.enabled,
                onChange = { EqualizerManager.setBandLevel(band, it.roundToInt()) },
            )
        }

        if (eq.bassSupported) {
            Text("Bass boost", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp))
            BandSlider(
                label = "Strength",
                valueLabel = "${eq.bassStrength / 10}%",
                value = eq.bassStrength.toFloat(),
                range = 0f..1000f,
                enabled = eq.enabled,
                onChange = { EqualizerManager.setBass(it.roundToInt()) },
            )
        }
        }
    }
}

@Composable
private fun BandSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onChange: (Float) -> Unit,
) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.labelMedium, color = Meow.TextLo)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            enabled = enabled,
            colors = SliderDefaults.colors(
                activeTrackColor = Meow.Coral,
                inactiveTrackColor = Meow.PanelHigh,
                disabledActiveTrackColor = Meow.Line,
                disabledInactiveTrackColor = Meow.PanelHigh,
            ),
            // Rectangular thumb to match the rest of the app.
            thumb = {
                Box(
                    Modifier.size(width = 10.dp, height = 28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (enabled) Brush.verticalGradient(listOf(Meow.Coral, Meow.Lilac)) else Brush.verticalGradient(listOf(Meow.Line, Meow.Line)))
                )
            },
        )
    }
}

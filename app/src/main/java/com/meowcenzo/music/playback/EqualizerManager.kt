package com.meowcenzo.music.playback

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EqState(
    val available: Boolean = false,
    val enabled: Boolean = false,
    val bandFreqsHz: List<Int> = emptyList(),
    val levels: List<Int> = emptyList(),      // millibels
    val minLevel: Int = -1500,
    val maxLevel: Int = 1500,
    val presets: List<String> = emptyList(),
    val bassStrength: Int = 0,                // 0..1000
    val bassSupported: Boolean = false,
)

/** Wraps the system Equalizer + BassBoost attached to ExoPlayer's audio session. */
object EqualizerManager {
    private var eq: Equalizer? = null
    private var bass: BassBoost? = null
    private var prefs: SharedPreferences? = null

    private val _state = MutableStateFlow(EqState())
    val state: StateFlow<EqState> = _state.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("meowcenzo_eq", Context.MODE_PRIVATE)
        }
    }

    fun attach(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        release()
        val p = prefs
        try {
            val e = Equalizer(0, audioSessionId)
            val range = e.bandLevelRange
            val bands = e.numberOfBands.toInt()
            val enabled = p?.getBoolean("enabled", false) ?: false
            val saved = p?.getString("levels", null)?.split(',')?.mapNotNull { it.toIntOrNull() }
            val levels = List(bands) { i ->
                saved?.getOrNull(i)?.coerceIn(range[0].toInt(), range[1].toInt())
                    ?: e.getBandLevel(i.toShort()).toInt()
            }
            levels.forEachIndexed { i, l -> e.setBandLevel(i.toShort(), l.toShort()) }
            e.setEnabled(enabled)
            eq = e

            var bassStrength = 0
            var bassSupported = false
            try {
                val b = BassBoost(0, audioSessionId)
                bassSupported = b.strengthSupported
                if (bassSupported) {
                    bassStrength = p?.getInt("bass", 0) ?: 0
                    b.setStrength(bassStrength.toShort())
                    b.setEnabled(enabled && bassStrength > 0)
                }
                bass = b
            } catch (_: Exception) {
                // Device has no bass boost.
            }

            _state.value = EqState(
                available = true,
                enabled = enabled,
                bandFreqsHz = List(bands) { e.getCenterFreq(it.toShort()) / 1000 },
                levels = levels,
                minLevel = range[0].toInt(),
                maxLevel = range[1].toInt(),
                presets = List(e.numberOfPresets.toInt()) { e.getPresetName(it.toShort()) },
                bassStrength = bassStrength,
                bassSupported = bassSupported,
            )
        } catch (_: Exception) {
            _state.value = EqState()
        }
    }

    fun toggle(on: Boolean) {
        try {
            eq?.setEnabled(on)
            bass?.setEnabled(on && _state.value.bassStrength > 0)
        } catch (_: Exception) { }
        prefs?.edit()?.putBoolean("enabled", on)?.apply()
        _state.update { it.copy(enabled = on) }
    }

    fun setBandLevel(band: Int, level: Int) {
        try { eq?.setBandLevel(band.toShort(), level.toShort()) } catch (_: Exception) { }
        _state.update { s ->
            val next = s.levels.toMutableList().also { if (band in it.indices) it[band] = level }
            persistLevels(next)
            s.copy(levels = next)
        }
    }

    fun applyPreset(index: Int) {
        val e = eq ?: return
        try {
            e.usePreset(index.toShort())
            val levels = List(_state.value.levels.size) { e.getBandLevel(it.toShort()).toInt() }
            persistLevels(levels)
            _state.update { it.copy(levels = levels) }
        } catch (_: Exception) { }
    }

    fun setBass(strength: Int) {
        try {
            bass?.setStrength(strength.toShort())
            bass?.setEnabled(_state.value.enabled && strength > 0)
        } catch (_: Exception) { }
        prefs?.edit()?.putInt("bass", strength)?.apply()
        _state.update { it.copy(bassStrength = strength) }
    }

    fun release() {
        try { eq?.release() } catch (_: Exception) { }
        try { bass?.release() } catch (_: Exception) { }
        eq = null
        bass = null
    }

    private fun persistLevels(levels: List<Int>) {
        prefs?.edit()?.putString("levels", levels.joinToString(","))?.apply()
    }
}

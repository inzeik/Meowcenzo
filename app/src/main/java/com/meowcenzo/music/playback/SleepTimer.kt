package com.meowcenzo.music.playback

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Counts down, then calls [onFinish] (the service pauses playback). */
object SleepTimer {
    private val handler = Handler(Looper.getMainLooper())
    private var endAt = 0L

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

    var onFinish: (() -> Unit)? = null

    private val tick = object : Runnable {
        override fun run() {
            val left = endAt - SystemClock.elapsedRealtime()
            if (left <= 0L) {
                _remainingMs.value = 0L
                onFinish?.invoke()
            } else {
                _remainingMs.value = left
                handler.postDelayed(this, 1000L)
            }
        }
    }

    fun start(minutes: Int) {
        cancel()
        endAt = SystemClock.elapsedRealtime() + minutes * 60_000L
        handler.post(tick)
    }

    fun cancel() {
        handler.removeCallbacks(tick)
        _remainingMs.value = 0L
    }
}

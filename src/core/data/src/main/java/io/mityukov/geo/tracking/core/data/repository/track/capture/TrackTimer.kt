package io.mityukov.geo.tracking.core.data.repository.track.capture

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.timer

internal class TrackTimer(val period: Long = 1000L, private val coroutineScope: CoroutineScope) {
    private val channel = Channel<Unit>()
    private val _events = MutableSharedFlow<Unit>()
    val events: Flow<Unit> = channel.consumeAsFlow()
    private var isPaused: Boolean = false
    private var timer: Timer? = null
    private var timerJob: Job? = null

    fun start() {
        timerJob = coroutineScope.launch {
            timer(period = period) {
                if (!isPaused) {
                    _events.tryEmit(Unit)
                }
            }
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
    }

    fun stop() {
        timer?.cancel()
        timer = null
    }
}

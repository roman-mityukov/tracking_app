package io.mityukov.geo.tracking.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PausableTimer(
    private val initialValue: Long = 0,
    private val interval: Long = 1000L,
    private val coroutineScope: CoroutineScope
) {
    private val _events = MutableStateFlow<Long>(0)
    val events: StateFlow<Long> = _events.asStateFlow()

    private var timerJob: Job? = null
    private var lastValue: Long = initialValue
    private var isPaused: Boolean = false

    private val commandChannel = Channel<Command>(Channel.UNLIMITED)

    sealed class Command {
        object Start : Command()
        object Pause : Command()
        object Resume : Command()
        object Stop : Command()
    }

    init {
        coroutineScope.launch {
            commandChannel.consumeAsFlow().collect { command ->
                when (command) {
                    is Command.Start -> startTimer()
                    is Command.Pause -> pauseTimer()
                    is Command.Resume -> resumeTimer()
                    is Command.Stop -> stopTimer()
                }
            }
        }
    }

    fun start() {
        commandChannel.trySend(Command.Start)
    }

    fun pause() {
        commandChannel.trySend(Command.Pause)
    }

    fun resume() {
        commandChannel.trySend(Command.Resume)
    }

    fun stop() {
        commandChannel.trySend(Command.Stop)
    }

    private fun startTimer() {
        if (timerJob != null) return

        lastValue = initialValue
        timerJob = coroutineScope.launch {
            while (true) {
                if (!isPaused) {
                    _events.update {
                        lastValue++
                    }
                }
                delay(interval)
            }
        }
    }

    private fun pauseTimer() {
        isPaused = true
    }

    private fun resumeTimer() {
        isPaused = false
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        lastValue = 0
        isPaused = false
    }
}

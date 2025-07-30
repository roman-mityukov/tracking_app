package io.mityukov.geo.tracking.feature.settings.geolocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.settings.app.LocalAppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

sealed interface GeolocationUpdatesIntervalEvent {
    data class SelectInterval(val interval: Duration) : GeolocationUpdatesIntervalEvent
}

sealed interface GeolocationUpdatesIntervalState {
    data object Pending : GeolocationUpdatesIntervalState
    data class Data(
        val interval: Duration,
        val availableIntervals: List<Duration> = listOf(
            10.seconds,
            20.seconds,
            30.seconds,
            45.seconds,
            1.minutes,
        ),
    ) : GeolocationUpdatesIntervalState
}

@HiltViewModel
class GeolocationUpdatesIntervalViewModel @Inject constructor(
    private val localAppSettingsRepository: LocalAppSettingsRepository
) : ViewModel() {
    val stateFlow = localAppSettingsRepository.localAppSettings.map { localAppSettings ->
        GeolocationUpdatesIntervalState.Data(
            interval = localAppSettings.geolocationUpdatesInterval,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        GeolocationUpdatesIntervalState.Pending
    )

    fun add(event: GeolocationUpdatesIntervalEvent) {
        when (event) {
            is GeolocationUpdatesIntervalEvent.SelectInterval -> {
                viewModelScope.launch {
                    localAppSettingsRepository.setGeolocationUpdatesRate(event.interval)
                }
            }
        }
    }
}

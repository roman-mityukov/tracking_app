@file:Suppress("MagicNumber")

package io.mityukov.geo.tracking.feature.settings.geolocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.common.CommonAppProps
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal sealed interface GeolocationSettingsEvent {
    data class SelectInterval(val interval: Duration) : GeolocationSettingsEvent
    data class SelectAccuracy(val accuracy: Int) : GeolocationSettingsEvent
    data class SelectSpeed(val speed: Int) : GeolocationSettingsEvent
}

internal sealed interface GeolocationSettingsState {
    data object Pending : GeolocationSettingsState
    data class Data(
        val interval: Duration,
        val availableIntervals: List<Duration> = listOf(
            3.seconds,
            10.seconds,
            20.seconds,
            30.seconds,
            45.seconds,
            1.minutes,
        ),
        val accuracy: Int,
        val availableAccuracy: List<Int> = listOf(
            0,
            1,
            5,
            10,
            20,
            30,
            40,
            50,
            100,
        ),
        val acceptableSpeed: Int,
        val availableAcceptableSpeed: List<Int> = listOf(
            1,
            5,
            10,
            20,
            30,
            40,
            50,
            60,
            70,
            80,
            90,
            100,
            110,
            120,
            130,
        ),
    ) : GeolocationSettingsState
}

@HiltViewModel
internal class GeolocationSettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {
    val stateFlow = appSettingsRepository.appSettings.map { localAppSettings ->
        GeolocationSettingsState.Data(
            interval = localAppSettings.geolocationUpdatesInterval,
            accuracy = localAppSettings.acceptableLocationAccuracy,
            acceptableSpeed = localAppSettings.acceptableDeviceVelocity,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = CommonAppProps.STOP_TIMEOUT_MILLISECONDS),
        GeolocationSettingsState.Pending
    )

    fun add(event: GeolocationSettingsEvent) {
        when (event) {
            is GeolocationSettingsEvent.SelectInterval -> {
                viewModelScope.launch {
                    appSettingsRepository.setGeolocationUpdatesRate(event.interval)
                }
            }

            is GeolocationSettingsEvent.SelectAccuracy -> {
                viewModelScope.launch {
                    appSettingsRepository.setAcceptableGeolocationAccuracy(event.accuracy)
                }
            }

            is GeolocationSettingsEvent.SelectSpeed -> {
                viewModelScope.launch {
                    appSettingsRepository.setAcceptableDeviceSpeed(event.speed)
                }
            }
        }
    }
}

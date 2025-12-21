package io.mityukov.geo.tracking.core.data.repository.settings.app

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class AppSettings(
    val showOnboarding: Boolean,
    val geolocationUpdatesInterval: Duration,
    val geolocationUpdatesDistance: Int,
    val acceptableDeviceVelocity: Int,
    val acceptableLocationAccuracy: Int,
) {
    companion object Defaults {
        val DEFAULT_GEOLOCATION_UPDATES_INTERVAL: Duration = 3.seconds
        const val DEFAULT_GEOLOCATION_UPDATES_DISTANCE: Int = 0 // meters
        const val DEFAULT_ACCEPTABLE_DEVICE_SPEED: Int  = 90 // kilometers per hour
        const val DEFAULT_ACCEPTABLE_LOCATION_ACCURACY: Int = 50 // meters
    }
}

package io.mityukov.geo.tracking.core.data.repository.settings.app

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class AppSettings(
    val showOnboarding: Boolean,
    val geolocationUpdatesInterval: Duration,
) {
    companion object Defaults {
        val GEOLOCATION_UPDATES_INTERVAL: Duration = 3.seconds
    }
}

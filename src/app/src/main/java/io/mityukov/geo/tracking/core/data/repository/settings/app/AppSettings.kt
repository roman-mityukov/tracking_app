package io.mityukov.geo.tracking.core.data.repository.settings.app

import kotlin.time.Duration

data class AppSettings(
    val showOnboarding: Boolean,
    val geolocationUpdatesInterval: Duration,
)

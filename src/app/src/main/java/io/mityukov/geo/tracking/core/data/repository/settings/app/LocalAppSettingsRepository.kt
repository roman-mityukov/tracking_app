package io.mityukov.geo.tracking.core.data.repository.settings.app

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface LocalAppSettingsRepository {
    suspend fun switchOnboarding()
    suspend fun setGeolocationUpdatesRate(duration: Duration)
    val localAppSettings: Flow<LocalAppSettings>
}

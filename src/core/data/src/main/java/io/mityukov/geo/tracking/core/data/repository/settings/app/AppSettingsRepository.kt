package io.mityukov.geo.tracking.core.data.repository.settings.app

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface AppSettingsRepository {
    suspend fun switchOnboarding()
    suspend fun setGeolocationUpdatesRate(duration: Duration)
    suspend fun resetToDefaults()
    val appSettings: Flow<AppSettings>
}

package io.mityukov.geo.tracking.core.data.repository.settings.app

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface AppSettingsRepository {
    suspend fun switchOnboarding()
    suspend fun setGeolocationUpdatesRate(duration: Duration)
    suspend fun setAcceptableGeolocationAccuracy(value: Int)
    suspend fun setAcceptableDeviceSpeed(value: Int)
    suspend fun resetToDefaults()
    val appSettings: Flow<AppSettings>
}

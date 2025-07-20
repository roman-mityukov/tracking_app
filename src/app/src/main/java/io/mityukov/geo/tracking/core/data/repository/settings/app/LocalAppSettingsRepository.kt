package io.mityukov.geo.tracking.core.data.repository.settings.app

import kotlinx.coroutines.flow.Flow

interface LocalAppSettingsRepository {
    suspend fun switchOnboarding()
    val localAppSettings: Flow<LocalAppSettings>
}

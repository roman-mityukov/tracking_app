package io.mityukov.geo.tracking.feature.splash

import androidx.lifecycle.ViewModel
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettings
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {
    suspend fun getAppSettings() : AppSettings {
        return appSettingsRepository.appSettings.first()
    }
}

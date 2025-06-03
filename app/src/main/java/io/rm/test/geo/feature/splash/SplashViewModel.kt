package io.rm.test.geo.feature.splash

import androidx.lifecycle.ViewModel
import io.rm.test.geo.core.data.repository.settings.app.LocalAppSettings
import io.rm.test.geo.core.data.repository.settings.app.LocalAppSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    private val localAppSettingsRepository: LocalAppSettingsRepository
) : ViewModel() {
    suspend fun getAppSettings() : LocalAppSettings {
        return localAppSettingsRepository.localAppSettings.first()
    }
}
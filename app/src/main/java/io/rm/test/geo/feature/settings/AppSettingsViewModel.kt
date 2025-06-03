package io.rm.test.geo.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.rm.test.geo.core.data.repository.settings.app.LocalAppSettings
import io.rm.test.geo.core.data.repository.settings.app.LocalAppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppSettingsEvent {
    data object SwitchOnboarding : AppSettingsEvent
}

sealed interface AppSettingsState {
    data object Pending : AppSettingsState
    data class Data(val localAppSettings: LocalAppSettings) : AppSettingsState
}

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val localAppSettingsRepository: LocalAppSettingsRepository,
) : ViewModel() {
    private val mutableStateFlow = MutableStateFlow<AppSettingsState>(AppSettingsState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            mutableStateFlow.update {
                AppSettingsState.Data(localAppSettingsRepository.localAppSettings.first())
            }
        }
    }

    fun add(event: AppSettingsEvent) {
        when (event) {
            AppSettingsEvent.SwitchOnboarding -> {
                viewModelScope.launch {
                    localAppSettingsRepository.switchOnboarding()

                    mutableStateFlow.update {
                        AppSettingsState.Data(localAppSettingsRepository.localAppSettings.first())
                    }
                }
            }
        }
    }
}
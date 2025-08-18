package io.mityukov.geo.tracking.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.core.data.repository.settings.app.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingEvent {
    data object ConsumeOnboarding : OnboardingEvent
}

sealed interface OnboardingState {
    data object Pending : OnboardingState
    data object OnboardingConsumed : OnboardingState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {
    private val mutableStateFlow = MutableStateFlow<OnboardingState>(OnboardingState.Pending)
    val stateFlow = mutableStateFlow.asStateFlow()

    fun add(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.ConsumeOnboarding -> {
                viewModelScope.launch {
                    appSettingsRepository.switchOnboarding()
                    mutableStateFlow.update {
                        OnboardingState.OnboardingConsumed
                    }
                }

            }
        }
    }
}

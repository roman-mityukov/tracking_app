package io.mityukov.geo.tracking.feature.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.feature.share.LogSharingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AboutEvent {
    data object ShareLogs : AboutEvent
    data object ConsumeLogs : AboutEvent
}

@HiltViewModel
class AboutViewModel @Inject constructor(private val logSharingService: LogSharingService) :
    ViewModel() {
    private val logsMutableStateFlow = MutableStateFlow<String?>(null)
    val logsStateFlow: StateFlow<String?> = logsMutableStateFlow.asStateFlow()

    fun add(event: AboutEvent) {
        when (event) {
            AboutEvent.ShareLogs -> {
                viewModelScope.launch {
                    logsMutableStateFlow.update {
                        logSharingService.prepareLogsFile()
                    }
                }
            }

            AboutEvent.ConsumeLogs -> {
                logsMutableStateFlow.update {
                    null
                }
            }
        }
    }
}

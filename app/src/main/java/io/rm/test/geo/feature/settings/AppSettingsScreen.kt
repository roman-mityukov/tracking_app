package io.rm.test.geo.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    when (state.value) {
        is AppSettingsState.Data -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Text("Настройки")
                    Spacer(modifier = Modifier.height(8.dp))
                    Switch(
                        checked = (state.value as AppSettingsState.Data).localAppSettings.showOnboarding,
                        onCheckedChange = {
                            viewModel.add(AppSettingsEvent.SwitchOnboarding)
                        })
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onBack) {
                        Text("Назад")
                    }
                }
            }
        }

        AppSettingsState.Pending -> {

        }
    }
}
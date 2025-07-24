package io.mityukov.geo.tracking.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel(), onNext: () -> Unit) {

    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    if (state.value is OnboardingState.OnboardingConsumed) {
        LaunchedEffect(Unit) {
            onNext()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Onboarding")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.add(OnboardingEvent.ConsumeOnboarding)
        }) {
            Text(text = "Дальше")
        }
    }
}

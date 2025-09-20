package io.mityukov.geo.tracking.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.mityukov.geo.tracking.core.ui.FontScalePreviews
import io.mityukov.geo.tracking.core.ui.NightModePreview

@Composable
internal fun OnboardingRoute(viewModel: OnboardingViewModel = hiltViewModel(), onNext: () -> Unit) {
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    if (state.value is OnboardingState.OnboardingConsumed) {
        LaunchedEffect(Unit) {
            onNext()
        }
    }

    OnboardingScreen(
        onConsumeOnboarding = {
            viewModel.add(OnboardingEvent.ConsumeOnboarding)
        },
    )
}

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onConsumeOnboarding: () -> Unit,
) {
    val density = LocalDensity.current

    val windowInfo = LocalWindowInfo.current
    val screenWidth = with(density) {
        windowInfo.containerSize.width.toDp()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                modifier = Modifier.size((screenWidth / 2)),
                painter = painterResource(R.drawable.feature_onboarding_ic_launcher_round),
                contentDescription = stringResource(R.string.feature_onboarding_content_description_app_icon)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.feature_onboarding_instructions_label),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.feature_onboarding_instructions_content),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onConsumeOnboarding) {
                Text(text = stringResource(R.string.feature_onboarding_button_label))
            }
        }
    }
}

@Preview
@FontScalePreviews
@NightModePreview
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(
        onConsumeOnboarding = {}
    )
}

package io.mityukov.geo.tracking.feature.onboarding.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import io.mityukov.geo.tracking.feature.onboarding.OnboardingPane
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute : NavKey


@Composable
fun OnboardingHost(
    onNext: () -> Unit,
) {
    OnboardingPane(onNext = onNext)
}

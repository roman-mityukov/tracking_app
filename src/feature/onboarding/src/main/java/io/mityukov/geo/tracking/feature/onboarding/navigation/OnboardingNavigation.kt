package io.mityukov.geo.tracking.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.mityukov.geo.tracking.feature.onboarding.OnboardingRoute
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingScreen(
    onNext: () -> Unit,
) {
    composable<OnboardingRoute> {
        OnboardingRoute(onNext = onNext)
    }
}

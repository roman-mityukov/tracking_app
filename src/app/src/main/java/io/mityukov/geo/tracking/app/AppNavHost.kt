package io.mityukov.geo.tracking.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.mityukov.geo.tracking.feature.map.navigation.MapRoute
import io.mityukov.geo.tracking.feature.onboarding.navigation.OnboardingRoute
import io.mityukov.geo.tracking.feature.onboarding.navigation.OnboardingHost

@Composable
fun AppNavHost(showOnboarding: Boolean) {
    val backStack = remember {
        mutableStateListOf(
            if (showOnboarding) OnboardingRoute else HomeRoute
        )
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<OnboardingRoute> {
                OnboardingHost(onNext = {
                    backStack.clear()
                    backStack.add(HomeRoute)
                })
            }
            entry<HomeRoute> { HomeScreen(MapRoute) }
        }
    )
}

package io.mityukov.geo.tracking.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import io.mityukov.geo.tracking.feature.map.navigation.MapRoute
import io.mityukov.geo.tracking.feature.onboarding.navigation.OnboardingRoute
import io.mityukov.geo.tracking.feature.onboarding.navigation.onboardingScreen

@Composable
fun AppNavHost(showOnboarding: Boolean) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (showOnboarding) OnboardingRoute else HomeRoute,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
    ) {
        onboardingScreen(onNext = {
            navController.navigate(HomeRoute) {
                popUpTo(OnboardingRoute) {
                    inclusive = true
                }
            }
        })
        composable<HomeRoute>(
            deepLinks = listOf(navDeepLink {
                uriPattern = DeepLinkProps.TRACK_DETAILS_URI_PATTERN
            })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString(DeepLinkProps.TRACK_DETAILS_PATH)
            HomeScreen(
                currentSelectedItem = if (trackId == null) {
                    MapRoute
                } else {
                    TracksParentRoute
                }
            )
        }
    }
}

package io.rm.test.geo.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.rm.test.geo.feature.home.HomeScreen
import io.rm.test.geo.feature.onboarding.OnboardingScreen
import kotlinx.serialization.Serializable

@Serializable
data object RouteOnboarding

@Serializable
data object RouteHome

@Composable
fun AppNavHost(showOnboarding: Boolean) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if (showOnboarding) RouteOnboarding else RouteHome,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
    ) {
        composable<RouteOnboarding> {
            OnboardingScreen(
                onNext = {
                    navController.navigate(RouteHome) {
                        popUpTo(RouteOnboarding) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable<RouteHome> {
            HomeScreen()
        }
    }
}
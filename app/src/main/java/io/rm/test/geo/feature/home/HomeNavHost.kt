package io.rm.test.geo.feature.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kotlinx.serialization.Serializable

@Serializable
data object HomeBaseRouteMap

@Serializable
data object HomeRouteMapCurrentLocation

@Serializable
data class HomeRoutePoiDetails(val poiId: String)

@Serializable
data object HomeBaseRouteTrack

@Serializable
data object HomeRouteTracksList

@Serializable
data object HomeBaseRouteProfile

@Serializable
data object HomeRouteProfile

@Serializable
data object HomeRouteSettings

@Composable
fun HomeNavHost(navController: NavHostController, snackbarHostState: SnackbarHostState) {
    NavHost(
        navController = navController,
        startDestination = HomeBaseRouteMap,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
    ) {
        mapScreenNavigation(
            onPoiSelected = {
                navController.navigate(HomeRoutePoiDetails(it))
            },
            onBack = {
                navController.popBackStack()
            },
            snackbarHostState = snackbarHostState,
        )
        tracksScreenNavigation()
        profileScreenNavigation(
            onSettingsSelected = {
                navController.navigate(HomeRouteSettings)
            },
            onBack = {
                navController.popBackStack()
            }
        )
    }
}
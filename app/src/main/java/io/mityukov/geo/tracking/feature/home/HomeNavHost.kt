package io.mityukov.geo.tracking.feature.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kotlinx.serialization.Serializable

sealed interface HomeBaseRoute {
    @Serializable
    data object HomeBaseRouteMap : HomeBaseRoute
    @Serializable
    data object HomeBaseRouteProfile : HomeBaseRoute
    @Serializable
    data object HomeBaseRouteTrack : HomeBaseRoute
}

@Serializable
data object HomeRouteMapCurrentLocation

@Serializable
data class HomeRoutePoiDetails(val poiId: String)

@Serializable
data object HomeRouteTracksList

@Serializable
data class HomeRouteTrackDetails(val trackId: String)

@Serializable
data class HomeRouteTracksEditing(val trackId: String)

@Serializable
data object HomeRouteProfile

@Serializable
data object HomeRouteSettings

@Composable
fun HomeNavHost(navController: NavHostController, snackbarHostState: SnackbarHostState) {
    NavHost(
        navController = navController,
        startDestination = HomeBaseRoute.HomeBaseRouteMap,
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
        tracksScreenNavigation(
            onTrackSelected = { trackId ->
                navController.navigate(HomeRouteTrackDetails(trackId))
            },
            onEditTracks = { firstSelectedTrackId ->
                navController.navigate(HomeRouteTracksEditing(firstSelectedTrackId))
            },
            onBack = {
                navController.popBackStack()
            },
        )
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
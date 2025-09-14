package io.mityukov.geo.tracking.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import io.mityukov.geo.tracking.feature.about.navigation.navigateToAbout
import io.mityukov.geo.tracking.feature.map.navigation.MapRoute
import io.mityukov.geo.tracking.feature.map.navigation.mapScreen
import io.mityukov.geo.tracking.feature.settings.navigation.navigateToAppSettings
import io.mityukov.geo.tracking.feature.settings.navigation.navigateToInstructions
import io.mityukov.geo.tracking.feature.track.details.navigation.navigateToTrackDetails
import io.mityukov.geo.tracking.feature.track.details.navigation.navigateToTrackDetailsMap
import io.mityukov.geo.tracking.feature.track.list.navigation.navigateToTracksEditing
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object ProfileParentRoute

@Serializable
data object TracksParentRoute


@Composable
fun HomeNavHost(navController: NavHostController, snackbarHostState: SnackbarHostState) {
    NavHost(
        navController = navController,
        startDestination = MapRoute,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
    ) {
        mapScreen(snackbarHostState = snackbarHostState)
        tracksScreenNavigation(
            onTrackSelected = { trackId ->
                navController.navigateToTrackDetails(trackId)
            },
            onTrackMapSelected = { trackId ->
                navController.navigateToTrackDetailsMap(trackId)
            },
            onEditTracks = { firstSelectedTrackId ->
                navController.navigateToTracksEditing(firstSelectedTrackId)
            },
            onBack = {
                navController.popBackStack()
            },
            snackbarHostState = snackbarHostState,
        )
        profileScreenNavigation(
            onSettingsSelected = {
                navController.navigateToAppSettings()
            },
            onAboutSelected = {
                navController.navigateToAbout()
            },
            onInstructionsSelected = {
                navController.navigateToInstructions()
            },
            onBack = {
                navController.popBackStack()
            },
            snackbarHostState = snackbarHostState
        )
    }
}

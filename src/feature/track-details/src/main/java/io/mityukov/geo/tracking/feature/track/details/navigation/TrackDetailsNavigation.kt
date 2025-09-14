package io.mityukov.geo.tracking.feature.track.details.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsMapScreen
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsRoute
import kotlinx.serialization.Serializable

@Serializable
data class TrackDetailsRoute(val trackId: String)

@Serializable
data class TrackDetailsMapRoute(val trackId: String)

fun NavController.navigateToTrackDetails(trackId: String) {
    navigate(TrackDetailsRoute(trackId))
}

fun NavController.navigateToTrackDetailsMap(trackId: String) {
    navigate(TrackDetailsMapRoute(trackId))
}

fun NavGraphBuilder.trackDetailsScreen(
    onTrackMapSelected: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    composable<TrackDetailsRoute>(
        deepLinks = listOf(navDeepLink {
            uriPattern = "geoapp://track/{trackId}"
        })
    ) {
        TrackDetailsRoute(
            onBack = onBack,
            onTrackMapSelected = onTrackMapSelected,
            snackbarHostState = snackbarHostState
        )
    }
}

fun NavGraphBuilder.trackDetailsMapScreen(onBack: () -> Unit) {
    composable<TrackDetailsMapRoute> {
        TrackDetailsMapScreen(onBack = onBack)
    }
}

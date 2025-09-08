package io.mityukov.geo.tracking.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import io.mityukov.geo.tracking.app.DeepLinkProps
import io.mityukov.geo.tracking.feature.about.AboutRoute
import io.mityukov.geo.tracking.feature.map.MapRoute
import io.mityukov.geo.tracking.feature.profile.ProfileScreen
import io.mityukov.geo.tracking.feature.settings.AppSettingsRoute
import io.mityukov.geo.tracking.feature.settings.instructions.InstructionsScreen
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsMapScreen
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsRoute
import io.mityukov.geo.tracking.feature.track.list.TracksRoute
import io.mityukov.geo.tracking.feature.track.list.editing.TracksEditingRoute

fun NavGraphBuilder.mapScreenNavigation(snackbarHostState: SnackbarHostState) {
    navigation<HomeBaseRoute.HomeBaseRouteMap>(startDestination = HomeRouteMapCurrentLocation) {
        composable<HomeRouteMapCurrentLocation> {
            MapRoute(snackbarHostState = snackbarHostState)
        }
    }
}

fun NavGraphBuilder.tracksScreenNavigation(
    onTrackSelected: (String) -> Unit,
    onTrackMapSelected: (String) -> Unit,
    onEditTracks: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    navigation<HomeBaseRoute.HomeBaseRouteTrack>(startDestination = HomeRouteTracksList) {
        composable<HomeRouteTracksList> {
            TracksRoute(
                onNavigateToTrack = onTrackSelected,
                onNavigateToTracksEditing = onEditTracks,
            )
        }
        composable<HomeRouteTrackDetails>(
            deepLinks = listOf(navDeepLink {
                uriPattern = DeepLinkProps.TRACK_DETAILS_URI_PATTERN
            })
        ) {
            TrackDetailsRoute(
                onBack = onBack,
                onTrackMapSelected = onTrackMapSelected,
                snackbarHostState = snackbarHostState
            )
        }
        composable<HomeRouteTrackDetailsMap> {
            TrackDetailsMapScreen(onBack = onBack)
        }
        composable<HomeRouteTracksEditing> {
            TracksEditingRoute(onBack = onBack)
        }
    }
}

fun NavGraphBuilder.profileScreenNavigation(
    onSettingsSelected: () -> Unit,
    onAboutSelected: () -> Unit,
    onInstructionsSelected: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    navigation<HomeBaseRoute.HomeBaseRouteProfile>(startDestination = HomeRouteProfile) {
        composable<HomeRouteProfile> {
            ProfileScreen(onSettingsSelected, onAboutSelected)
        }
        composable<HomeRouteSettings> {
            AppSettingsRoute(onInstructionsSelected = onInstructionsSelected, onBack = onBack)
        }
        composable<HomeRouteAbout> {
            AboutRoute(onBack = onBack, snackbarHostState = snackbarHostState)
        }
        composable<HomeRouteInstructions> {
            InstructionsScreen(onBack = onBack)
        }
    }
}

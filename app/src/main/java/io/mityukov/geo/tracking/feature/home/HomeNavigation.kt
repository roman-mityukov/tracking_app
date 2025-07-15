package io.mityukov.geo.tracking.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import io.mityukov.geo.tracking.app.DeepLinkProps
import io.mityukov.geo.tracking.feature.map.MapScreen
import io.mityukov.geo.tracking.feature.poi.PoiDetailsScreen
import io.mityukov.geo.tracking.feature.profile.ProfileScreen
import io.mityukov.geo.tracking.feature.settings.AppSettingsScreen
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsScreen
import io.mityukov.geo.tracking.feature.track.list.TracksScreen
import io.mityukov.geo.tracking.feature.track.list.editing.TracksEditingScreen

fun NavGraphBuilder.mapScreenNavigation(
    onPoiSelected: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    navigation<HomeBaseRoute.HomeBaseRouteMap>(startDestination = HomeRouteMapCurrentLocation) {
        composable<HomeRouteMapCurrentLocation> {
            MapScreen(onPoiSelected = onPoiSelected, snackbarHostState = snackbarHostState)
        }
        composable<HomeRoutePoiDetails> {
            PoiDetailsScreen(onBack = onBack)
        }
    }
}

fun NavGraphBuilder.tracksScreenNavigation(
    onTrackSelected: (String) -> Unit,
    onEditTracks: (String) -> Unit,
    onBack: () -> Unit,
) {
    navigation<HomeBaseRoute.HomeBaseRouteTrack>(startDestination = HomeRouteTracksList) {
        composable<HomeRouteTracksList> {
            TracksScreen(
                onNavigateToTrack = onTrackSelected,
                onNavigateToTracksEditing = onEditTracks,
            )
        }
        composable<HomeRouteTrackDetails>(
            deepLinks = listOf(navDeepLink {
                uriPattern = DeepLinkProps.TRACK_DETAILS_URI_PATTERN
            })
        ) {
            TrackDetailsScreen(onBack = onBack)
        }
        composable<HomeRouteTracksEditing> {
            TracksEditingScreen(onBack = onBack)
        }
    }
}

fun NavGraphBuilder.profileScreenNavigation(onSettingsSelected: () -> Unit, onBack: () -> Unit) {
    navigation<HomeBaseRoute.HomeBaseRouteProfile>(startDestination = HomeRouteProfile) {
        composable<HomeRouteProfile> {
            ProfileScreen(onSettingsSelected)
        }
        composable<HomeRouteSettings> {
            AppSettingsScreen(onBack = onBack)
        }
    }
}
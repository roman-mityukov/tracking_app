package io.mityukov.geo.tracking.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.mityukov.geo.tracking.feature.map.MapScreen
import io.mityukov.geo.tracking.feature.poi.PoiDetailsScreen
import io.mityukov.geo.tracking.feature.profile.ProfileScreen
import io.mityukov.geo.tracking.feature.settings.AppSettingsScreen
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsScreen
import io.mityukov.geo.tracking.feature.track.list.TracksListScreen

fun NavGraphBuilder.mapScreenNavigation(
    onPoiSelected: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    navigation<HomeBaseRouteMap>(startDestination = HomeRouteMapCurrentLocation) {
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
    onBack: () -> Unit,
) {
    navigation<HomeBaseRouteTrack>(startDestination = HomeRouteTracksList) {
        composable<HomeRouteTracksList> {
            TracksListScreen(onTrackSelected = onTrackSelected)
        }
        composable<HomeRouteTrackDetails> {
            TrackDetailsScreen(onBack = onBack)
        }
    }
}

fun NavGraphBuilder.profileScreenNavigation(onSettingsSelected: () -> Unit, onBack: () -> Unit) {
    navigation<HomeBaseRouteProfile>(startDestination = HomeRouteProfile) {
        composable<HomeRouteProfile> {
            ProfileScreen(onSettingsSelected)
        }
        composable<HomeRouteSettings> {
            AppSettingsScreen(onBack = onBack)
        }
    }
}
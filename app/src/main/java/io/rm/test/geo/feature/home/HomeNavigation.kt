package io.rm.test.geo.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.rm.test.geo.feature.map.MapScreen
import io.rm.test.geo.feature.poi.PoiDetailsScreen
import io.rm.test.geo.feature.profile.ProfileScreen
import io.rm.test.geo.feature.settings.AppSettingsScreen
import io.rm.test.geo.feature.track.list.TracksListScreen

fun NavGraphBuilder.mapScreenNavigation(onPoiSelected: (String) -> Unit, onBack: () -> Unit) {
    navigation<HomeBaseRouteMap>(startDestination = HomeRouteMapCurrentLocation) {
        composable<HomeRouteMapCurrentLocation> {
            MapScreen(onPoiSelected = onPoiSelected)
        }
        composable<HomeRoutePoiDetails> {
            PoiDetailsScreen(onBack = onBack)
        }
    }
}

fun NavGraphBuilder.tracksScreenNavigation() {
    navigation<HomeBaseRouteTrack>(startDestination = HomeRouteTracksList) {
        composable<HomeRouteTracksList> {
            TracksListScreen()
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
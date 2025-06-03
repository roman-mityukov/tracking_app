package io.rm.test.geo.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.rm.test.geo.feature.map.MapScreen
import io.rm.test.geo.feature.poi.PoiDetailsScreen
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
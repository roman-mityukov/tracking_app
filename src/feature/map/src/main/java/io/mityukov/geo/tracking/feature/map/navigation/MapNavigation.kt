package io.mityukov.geo.tracking.feature.map.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.mityukov.geo.tracking.feature.map.MapRoute
import kotlinx.serialization.Serializable

@Serializable
data object MapRoute

fun NavGraphBuilder.mapScreen(snackbarHostState: SnackbarHostState) {
    composable<MapRoute> {
        MapRoute(snackbarHostState = snackbarHostState)
    }
}

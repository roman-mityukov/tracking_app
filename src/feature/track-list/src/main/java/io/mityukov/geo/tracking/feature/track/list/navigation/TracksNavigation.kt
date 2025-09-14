package io.mityukov.geo.tracking.feature.track.list.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.mityukov.geo.tracking.feature.track.list.TracksRoute
import io.mityukov.geo.tracking.feature.track.list.editing.TracksEditingRoute
import kotlinx.serialization.Serializable

@Serializable
data class TracksEditingRoute(val trackId: String)

@Serializable
data object TracksListRoute

fun NavController.navigateToTracksEditing(firstSelectedTrackId: String) {
    navigate(TracksEditingRoute(firstSelectedTrackId))
}

fun NavGraphBuilder.tracksScreen(
    onTrackSelected: (String) -> Unit,
    onEditTracks: (String) -> Unit,
) {
    composable<TracksListRoute> {
        TracksRoute(
            onNavigateToTrack = onTrackSelected,
            onNavigateToTracksEditing = onEditTracks,
        )
    }
}

fun NavGraphBuilder.tracksEditingScreen(
    onBack: () -> Unit,
) {
    composable<TracksEditingRoute> {
        TracksEditingRoute(onBack = onBack)
    }
}

package io.mityukov.geo.tracking.feature.track.details.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsMapPane
import io.mityukov.geo.tracking.feature.track.details.TrackDetailsHost
import kotlinx.serialization.Serializable

@Serializable
data class TrackDetailsRoute(val trackId: String) : NavKey

@Serializable
data class TrackDetailsMapRoute(val trackId: String) : NavKey

fun EntryProviderScope<Any>.trackDetailsNavigation(
    snackbarHostState: SnackbarHostState,
    backStack: NavBackStack<NavKey>,
) {
    val onBack = {
        backStack.removeLastOrNull()
        Unit
    }
    entry<TrackDetailsRoute> { navKey ->
        TrackDetailsHost(
            navKey = navKey,
            onBack = onBack,
            onTrackMapSelected = { trackId ->
                backStack.add(TrackDetailsMapRoute(trackId))
            },
            snackbarHostState = snackbarHostState
        )
    }
    entry<TrackDetailsMapRoute> { navKey ->
        TrackDetailsMapPane(navKey = navKey, onBack = onBack)
    }
}

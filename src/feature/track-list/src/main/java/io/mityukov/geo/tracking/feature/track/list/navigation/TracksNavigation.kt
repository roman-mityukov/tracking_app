package io.mityukov.geo.tracking.feature.track.list.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.mityukov.geo.tracking.feature.track.list.TracksListPane
import io.mityukov.geo.tracking.feature.track.list.editing.TracksEditingPane
import kotlinx.serialization.Serializable

@Serializable
data object TracksHostRoute : NavKey

@Serializable
data class TracksEditingRoute(val trackId: String) : NavKey

@Serializable
data object TracksListRoute : NavKey

@Composable
fun TracksHost(
    snackbarHostState: SnackbarHostState,
    backStack: NavBackStack<NavKey>,
    trackDetailsEntryProvider: EntryProviderScope<Any>.() -> Unit,
    onTrackDetails: (String) -> Unit,
    onBack: () -> Unit,
) {
    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size == 1) {
                onBack()
            } else {
                backStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider<Any> {
            entry<TracksListRoute> {
                TracksListPane(
                    onNavigateToTrack = { trackId ->
                        onTrackDetails(trackId)
                    },
                    onNavigateToTracksEditing = { trackId ->
                        backStack.add(TracksEditingRoute(trackId))
                    },
                )
            }
            entry<TracksEditingRoute> { navKey ->
                TracksEditingPane(
                    navKey = navKey,
                    snackbarHostState = snackbarHostState,
                    onBack = {
                        backStack.removeLastOrNull()
                    },
                )
            }
            this.trackDetailsEntryProvider()
        }
    )
}

package io.mityukov.geo.tracking.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.mityukov.geo.tracking.feature.about.navigation.AboutRoute
import io.mityukov.geo.tracking.feature.about.navigation.aboutNavigation
import io.mityukov.geo.tracking.feature.map.navigation.MapRoute
import io.mityukov.geo.tracking.feature.map.navigation.MapHost
import io.mityukov.geo.tracking.feature.profile.navigation.ProfileHost
import io.mityukov.geo.tracking.feature.profile.navigation.ProfileHostRoute
import io.mityukov.geo.tracking.feature.profile.navigation.ProfileRoute
import io.mityukov.geo.tracking.feature.settings.navigation.AppSettingsRoute
import io.mityukov.geo.tracking.feature.settings.navigation.InstructionsRoute
import io.mityukov.geo.tracking.feature.settings.navigation.settingsNavigation
import io.mityukov.geo.tracking.feature.track.details.navigation.TrackDetailsRoute
import io.mityukov.geo.tracking.feature.track.details.navigation.trackDetailsNavigation
import io.mityukov.geo.tracking.feature.track.list.navigation.TracksListRoute
import io.mityukov.geo.tracking.feature.track.list.navigation.TracksHostRoute
import io.mityukov.geo.tracking.feature.track.list.navigation.TracksHost
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Composable
fun HomeNavHost(
    backStack: SnapshotStateList<Any>,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryProvider = entryProvider {
            entry<MapRoute> {
                MapHost(snackbarHostState = snackbarHostState)
            }
            entry<TracksHostRoute> {
                val tracksBackStack = rememberNavBackStack(TracksListRoute)
                TracksHost(
                    snackbarHostState = snackbarHostState,
                    backStack = tracksBackStack,
                    trackDetailsEntryProvider = {
                        trackDetailsNavigation(
                            backStack = tracksBackStack,
                            snackbarHostState = snackbarHostState,
                        )
                    },
                    onTrackDetails = { trackId ->
                        tracksBackStack.add(TrackDetailsRoute(trackId))
                    },
                    onBack = onBack,
                )
            }
            entry<ProfileHostRoute> {
                val profileBackStack = rememberNavBackStack(ProfileRoute)
                ProfileHost(
                    backStack = profileBackStack,
                    aboutEntryProvider = {
                        aboutNavigation(
                            snackbarHostState = snackbarHostState,
                            onBack = {
                                profileBackStack.removeLastOrNull()
                            }
                        )
                    },
                    onAboutSelected = {
                        profileBackStack.add(AboutRoute)
                    },
                    settingsEntryProvider = {
                        settingsNavigation(
                            onInstructionsSelected = {
                                profileBackStack.add(InstructionsRoute)
                            },
                            onBack = {
                                profileBackStack.removeLastOrNull()
                            }
                        )
                    },
                    onSettingsSelected = {
                        profileBackStack.add(AppSettingsRoute)
                    },
                    onBack = onBack
                )
            }
        }
    )
}

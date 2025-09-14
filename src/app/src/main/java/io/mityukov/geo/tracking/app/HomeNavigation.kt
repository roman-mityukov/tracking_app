package io.mityukov.geo.tracking.app

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import io.mityukov.geo.tracking.feature.about.navigation.aboutScreen
import io.mityukov.geo.tracking.feature.profile.navigation.ProfileRoute
import io.mityukov.geo.tracking.feature.profile.navigation.profileScreen
import io.mityukov.geo.tracking.feature.settings.navigation.appSettingsScreen
import io.mityukov.geo.tracking.feature.settings.navigation.instructionsScreen
import io.mityukov.geo.tracking.feature.track.details.navigation.trackDetailsMapScreen
import io.mityukov.geo.tracking.feature.track.details.navigation.trackDetailsScreen
import io.mityukov.geo.tracking.feature.track.list.navigation.TracksListRoute
import io.mityukov.geo.tracking.feature.track.list.navigation.tracksEditingScreen
import io.mityukov.geo.tracking.feature.track.list.navigation.tracksScreen

fun NavGraphBuilder.tracksScreenNavigation(
    onTrackSelected: (String) -> Unit,
    onEditTracks: (String) -> Unit,
    onTrackMapSelected: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    navigation<TracksParentRoute>(startDestination = TracksListRoute) {
        tracksScreen(onTrackSelected, onEditTracks)
        trackDetailsScreen(onTrackMapSelected, onBack, snackbarHostState)
        trackDetailsMapScreen(onBack)
        tracksEditingScreen(onBack)
    }
}

fun NavGraphBuilder.profileScreenNavigation(
    onSettingsSelected: () -> Unit,
    onAboutSelected: () -> Unit,
    onInstructionsSelected: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    navigation<ProfileParentRoute>(startDestination = ProfileRoute) {
        profileScreen(onSettingsSelected, onAboutSelected)
        appSettingsScreen(onInstructionsSelected, onBack)
        aboutScreen(onBack, snackbarHostState)
        instructionsScreen(onBack)
    }
}

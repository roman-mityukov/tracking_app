package io.mityukov.geo.tracking.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.mityukov.geo.tracking.feature.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProfileRoute

fun NavGraphBuilder.profileScreen(
    onSettingsSelected: () -> Unit,
    onAboutSelected: () -> Unit,
) {
    composable<ProfileRoute> {
        ProfileScreen(onSettingsSelected, onAboutSelected)
    }
}

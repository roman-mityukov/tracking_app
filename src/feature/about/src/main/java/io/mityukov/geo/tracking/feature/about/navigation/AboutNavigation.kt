package io.mityukov.geo.tracking.feature.about.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.mityukov.geo.tracking.feature.about.AboutRoute
import kotlinx.serialization.Serializable

@Serializable
data object AboutRoute

fun NavController.navigateToAbout() {
    navigate(AboutRoute)
}

fun NavGraphBuilder.aboutScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    composable<AboutRoute> {
        AboutRoute(onBack = onBack, snackbarHostState = snackbarHostState)
    }
}

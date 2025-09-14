package io.mityukov.geo.tracking.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.mityukov.geo.tracking.feature.settings.AppSettingsRoute
import io.mityukov.geo.tracking.feature.settings.instructions.InstructionsScreen
import kotlinx.serialization.Serializable

@Serializable
data object AppSettingsRoute

@Serializable
data object InstructionsRoute

fun NavController.navigateToAppSettings() {
    navigate(AppSettingsRoute)
}

fun NavController.navigateToInstructions() {
    navigate(InstructionsRoute)
}

fun NavGraphBuilder.appSettingsScreen(
    onInstructionsSelected: () -> Unit,
    onBack: () -> Unit,
) {
    composable<AppSettingsRoute> {
        AppSettingsRoute(onInstructionsSelected = onInstructionsSelected, onBack = onBack)
    }
}

fun NavGraphBuilder.instructionsScreen(
    onBack: () -> Unit,
) {
    composable<InstructionsRoute> {
        InstructionsScreen(onBack = onBack)
    }
}

package io.mityukov.geo.tracking.feature.about.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.mityukov.geo.tracking.feature.about.AboutPane
import kotlinx.serialization.Serializable

@Serializable
data object AboutRoute : NavKey

fun EntryProviderScope<Any>.aboutNavigation(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    entry<AboutRoute> {
        AboutPane(snackbarHostState = snackbarHostState, onBack = onBack)
    }
}

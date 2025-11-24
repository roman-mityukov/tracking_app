package io.mityukov.geo.tracking.feature.map.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import io.mityukov.geo.tracking.feature.map.MapPane
import kotlinx.serialization.Serializable

@Serializable
data object MapRoute : NavKey

@Composable
fun MapHost(snackbarHostState: SnackbarHostState) {
    MapPane(snackbarHostState = snackbarHostState)
}

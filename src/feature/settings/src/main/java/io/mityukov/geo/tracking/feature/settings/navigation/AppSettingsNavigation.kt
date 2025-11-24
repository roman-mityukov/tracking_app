package io.mityukov.geo.tracking.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.mityukov.geo.tracking.feature.settings.AppSettingsPane
import io.mityukov.geo.tracking.feature.settings.instructions.InstructionsPane
import kotlinx.serialization.Serializable

@Serializable
data object AppSettingsRoute : NavKey

@Serializable
data object InstructionsRoute : NavKey

fun EntryProviderScope<Any>.settingsNavigation(
    onInstructionsSelected: () -> Unit,
    onBack: () -> Unit
) {
    entry<AppSettingsRoute> {
        AppSettingsPane(onInstructionsSelected = onInstructionsSelected, onBack = onBack)
    }
    entry<InstructionsRoute> {
        InstructionsPane(onBack = onBack)
    }
}

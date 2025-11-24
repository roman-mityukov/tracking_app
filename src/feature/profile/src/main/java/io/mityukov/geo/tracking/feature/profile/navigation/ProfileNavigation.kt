package io.mityukov.geo.tracking.feature.profile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.mityukov.geo.tracking.feature.profile.ProfilePane
import kotlinx.serialization.Serializable

@Serializable
data object ProfileHostRoute : NavKey

@Serializable
data object ProfileRoute : NavKey

@Composable
fun ProfileHost(
    backStack: NavBackStack<NavKey>,
    aboutEntryProvider: EntryProviderScope<Any>.() -> Unit,
    settingsEntryProvider: EntryProviderScope<Any>.() -> Unit,
    onAboutSelected: () -> Unit,
    onSettingsSelected: () -> Unit,
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
        entryProvider = entryProvider<Any> {
            entry<ProfileRoute> {
                ProfilePane(onSettingsSelected, onAboutSelected)
            }
            this.aboutEntryProvider()
            this.settingsEntryProvider()
        },
    )
}

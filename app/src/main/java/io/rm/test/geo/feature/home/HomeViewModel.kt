package io.rm.test.geo.feature.home

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.rm.test.geo.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeNavigationItem(
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val label: Int,
)

sealed interface HomeState {
    data class Success(val navigationItems: List<HomeNavigationItem>) : HomeState
}

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val mutableStateFlow = MutableStateFlow(
        HomeState.Success(
            listOf(
                HomeNavigationItem(
                    route = HomeBaseRouteMap,
                    selectedIcon = Icons.Filled.Build,
                    unselectedIcon = Icons.Outlined.Build,
                    label = R.string.home_navigation_map,
                ),
                HomeNavigationItem(
                    route = HomeBaseRouteTrack,
                    selectedIcon = Icons.Filled.Person,
                    unselectedIcon = Icons.Outlined.Person,
                    label = R.string.home_navigation_user_tracks,
                ),
            )
        ),
    )
    val stateFlow = mutableStateFlow.asStateFlow()
}
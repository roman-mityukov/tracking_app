package io.rm.test.geo.feature.home

import androidx.annotation.DrawableRes
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
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
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
                    selectedIcon = R.drawable.home_map_filled,
                    unselectedIcon = R.drawable.home_map_outlined,
                    label = R.string.home_navigation_map,
                ),
                HomeNavigationItem(
                    route = HomeBaseRouteTrack,
                    selectedIcon = R.drawable.home_track_filled,
                    unselectedIcon = R.drawable.home_track_outlined,
                    label = R.string.home_navigation_user_tracks,
                ),
                HomeNavigationItem(
                    route = HomeBaseRouteProfile,
                    selectedIcon = R.drawable.home_profile_filled,
                    unselectedIcon = R.drawable.home_profile_outlined,
                    label = R.string.home_navigation_user_profile,
                ),
            )
        ),
    )
    val stateFlow = mutableStateFlow.asStateFlow()
}
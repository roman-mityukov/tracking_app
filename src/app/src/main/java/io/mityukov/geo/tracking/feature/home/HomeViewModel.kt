package io.mityukov.geo.tracking.feature.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.mityukov.geo.tracking.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeNavigationItem(
    val route: HomeBaseRoute,
    @param:DrawableRes val selectedIcon: Int,
    @param:DrawableRes val unselectedIcon: Int,
    @param:StringRes val label: Int,
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
                    route = HomeBaseRoute.HomeBaseRouteMap,
                    selectedIcon = R.drawable.home_map_filled,
                    unselectedIcon = R.drawable.home_map_outlined,
                    label = R.string.home_navigation_map,
                ),
                HomeNavigationItem(
                    route = HomeBaseRoute.HomeBaseRouteTrack,
                    selectedIcon = R.drawable.home_track_filled,
                    unselectedIcon = R.drawable.home_track_outlined,
                    label = R.string.home_navigation_user_tracks,
                ),
                HomeNavigationItem(
                    route = HomeBaseRoute.HomeBaseRouteProfile,
                    selectedIcon = R.drawable.home_profile_filled,
                    unselectedIcon = R.drawable.home_profile_outlined,
                    label = R.string.home_navigation_user_profile,
                ),
            )
        ),
    )
    val stateFlow = mutableStateFlow.asStateFlow()
}

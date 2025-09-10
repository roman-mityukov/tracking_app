package io.mityukov.geo.tracking.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.designsystem.icon.AppIcons
import io.mityukov.geo.tracking.feature.map.navigation.MapRoute

private data class HomeNavigationItem(
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: Int,
)

private fun buildNavigationItems(): List<HomeNavigationItem> {
    return listOf(
        HomeNavigationItem(
            route = MapRoute,
            selectedIcon = AppIcons.HomeMapFilled,
            unselectedIcon = AppIcons.HomeMapOutlined,
            label = R.string.home_navigation_map,
        ),
        HomeNavigationItem(
            route = TracksParentRoute,
            selectedIcon = AppIcons.HomeTrackFilled,
            unselectedIcon = AppIcons.HomeTrackOutlined,
            label = R.string.home_navigation_user_tracks,
        ),
        HomeNavigationItem(
            route = ProfileParentRoute,
            selectedIcon = AppIcons.HomeProfileFilled,
            unselectedIcon = AppIcons.HomeProfileOutlined,
            label = R.string.home_navigation_user_profile,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(currentSelectedItem: Any) {
    val navController = rememberNavController()

    val navigationItems = buildNavigationItems()

    val routes = navigationItems.map { it.route }
    var selectedItem by rememberSaveable { mutableIntStateOf(routes.indexOf(currentSelectedItem)) }
    var oldItem = selectedItem

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedItem == index) {
                                    item.selectedIcon
                                } else {
                                    item.unselectedIcon
                                },
                                contentDescription = stringResource(item.label)
                            )
                        },
                        label = { Text(stringResource(item.label)) },
                        selected = selectedItem == index,
                        onClick = {
                            if (selectedItem == index) {
                                navController.navigate(routes[selectedItem]) {
                                    popUpTo(routes[selectedItem]) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                oldItem = selectedItem
                                selectedItem = index

                                navController.navigate(routes[selectedItem]) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(routes[oldItem]) {
                                        inclusive = true
                                        saveState = true
                                    }
                                }
                            }
                        }
                    )
                }
            }
        },
    ) { innerPadding ->
        val bottomPadding = innerPadding.calculateBottomPadding()
        Box(
            modifier = Modifier
                .padding(bottom = bottomPadding)
                .consumeWindowInsets(PaddingValues(bottom = bottomPadding))
        ) {
            HomeNavHost(navController, snackbarHostState)
        }
    }
}

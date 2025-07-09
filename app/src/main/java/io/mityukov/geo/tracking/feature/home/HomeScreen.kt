package io.mityukov.geo.tracking.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }
    var oldItem = selectedItem
    val state = viewModel.stateFlow.collectAsStateWithLifecycle()
    val routes = state.value.navigationItems.map { it.route }
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            NavigationBar {
                state.value.navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(if (selectedItem == index) item.selectedIcon else item.unselectedIcon),
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
        contentWindowInsets = WindowInsets.safeContent,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(
                    bottom = innerPadding.calculateBottomPadding(),
                )
                .fillMaxSize()
        ) {
            HomeNavHost(navController, snackbarHostState)
        }
    }
}
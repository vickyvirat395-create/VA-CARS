package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.ThemePreference
import com.example.ui.VaCarsViewModel
import com.example.ui.components.BrandHeader
import com.example.ui.components.SetupModal
import com.example.ui.screens.CarDetailsScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.FleetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.VaCarsTheme
import kotlinx.coroutines.flow.collectLatest

private const val ROUTE_HOME = "home"
private const val ROUTE_FLEET = "fleet"
private const val ROUTE_CONTACT = "contact"
private const val ROUTE_CAR_DETAIL = "car_detail/{carId}"

private data class NavTabItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TopLevelTabs = listOf(
    NavTabItem(
        route = ROUTE_HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    NavTabItem(
        route = ROUTE_FLEET,
        labelRes = R.string.nav_fleet,
        selectedIcon = Icons.Filled.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar,
        testTag = "nav_tab_fleet"
    ),
    NavTabItem(
        route = ROUTE_CONTACT,
        labelRes = R.string.nav_contact,
        selectedIcon = Icons.Filled.Call,
        unselectedIcon = Icons.Outlined.Call,
        testTag = "nav_tab_contact"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaCarsApp()
        }
    }
}

@Composable
fun VaCarsApp(
    viewModel: VaCarsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (uiState.themePreference) {
        ThemePreference.SYSTEM -> systemDark
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
    }

    VaCarsTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val isDetailRoute = currentDestination?.route == ROUTE_CAR_DETAIL

        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(viewModel) {
            viewModel.snackbarEvents.collectLatest { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        val navigateToTab: (String) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        val openCarDetails: (String) -> Unit = { carId ->
            navController.navigate("car_detail/${Uri.encode(carId)}")
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 720.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    if (!isDetailRoute) {
                        BrandHeader(
                            dataSource = uiState.dataSource,
                            isLoading = uiState.isLoading,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleTheme(isDarkTheme) },
                            onOpenSetup = { viewModel.openSetupModal() }
                        )
                    }
                },
                bottomBar = {
                    if (!isDetailRoute && !useNavigationRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            TopLevelTabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == tab.route
                                } == true
                                val label = stringResource(tab.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (!isDetailRoute && useNavigationRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            TopLevelTabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.route == tab.route
                                } == true
                                val label = stringResource(tab.labelRes)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_HOME,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(ROUTE_HOME) {
                                HomeScreen(
                                    uiState = uiState,
                                    onSearchQueryChange = viewModel::onSearchQueryChange,
                                    onFilterSelected = viewModel::onFilterSelected,
                                    onClearFilters = viewModel::clearSearchAndFilters,
                                    onRefresh = viewModel::refreshFleet,
                                    onExploreFleet = { navigateToTab(ROUTE_FLEET) },
                                    onCarClick = { car -> openCarDetails(car.id) },
                                    onOpenSetup = viewModel::openSetupModal
                                )
                            }

                            composable(ROUTE_FLEET) {
                                FleetScreen(
                                    uiState = uiState,
                                    onSearchQueryChange = viewModel::onSearchQueryChange,
                                    onFilterSelected = viewModel::onFilterSelected,
                                    onClearFilters = viewModel::clearSearchAndFilters,
                                    onRefresh = viewModel::refreshFleet,
                                    onCarClick = { car -> openCarDetails(car.id) },
                                    onOpenSetup = viewModel::openSetupModal
                                )
                            }

                            composable(ROUTE_CONTACT) {
                                ContactScreen(
                                    whatsappNumber = uiState.config.whatsappNumber,
                                    onOpenSetup = viewModel::openSetupModal,
                                    onShowMessage = viewModel::notifyMessage
                                )
                            }

                            composable(
                                route = ROUTE_CAR_DETAIL,
                                arguments = listOf(navArgument("carId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val rawId = backStackEntry.arguments?.getString("carId").orEmpty()
                                val decodedId = Uri.decode(rawId)
                                val car = viewModel.findCarById(decodedId)
                                CarDetailsScreen(
                                    car = car,
                                    whatsappNumber = uiState.config.whatsappNumber,
                                    onBack = { navController.popBackStack() },
                                    onShowMessage = viewModel::notifyMessage
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isSetupModalOpen) {
                SetupModal(
                    currentConfig = uiState.config,
                    dataSource = uiState.dataSource,
                    errorMessage = uiState.errorMessage,
                    onDismiss = viewModel::closeSetupModal,
                    onSaveAndConnect = viewModel::saveSupabaseConfig,
                    onUseSampleData = viewModel::resetToSampleData
                )
            }
        }
    }
}

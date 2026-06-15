package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogFoodScreen
import com.example.ui.screens.BarcodeScannerScreen
import com.example.ui.screens.ManualAddScreen
import com.example.ui.screens.WeeklySummaryScreen
import com.example.ui.screens.PaywallScreen
import com.example.ui.screens.AICoachScreen
import com.example.ui.screens.FoodDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NutritionViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Construct viewModel connected to local application context
        val viewModel = ViewModelProvider(
            this,
            NutritionViewModel.Factory(application)
        )[NutritionViewModel::class.java]

        val dataString = intent?.dataString
        val action = intent?.action
        val hasScanDeepLink = (dataString != null && (dataString.startsWith("nutrilens://scan") || dataString.contains("scan"))) ||
                intent?.getStringExtra("navigate_to") == "barcode_scanner" ||
                action == "actions.intent.ADD_TO_INVENTORY"

        var startDest = "dashboard"
        if (hasScanDeepLink) {
            var mealType = "BREAKFAST"
            try {
                intent?.data?.let { uri ->
                    val mealQuery = uri.getQueryParameter("meal") ?: uri.getQueryParameter("mealType")
                    if (!mealQuery.isNullOrBlank()) {
                        mealType = mealQuery.uppercase()
                    }
                }
            } catch (e: Exception) {
                // fallback to default
            }
            startDest = "barcode_scanner/$mealType"
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dynamicColor by viewModel.dynamicColorEnabled.collectAsState()

            MyApplicationTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                MainAppNavigation(
                    viewModel = viewModel,
                    widthSizeClass = windowSizeClass.widthSizeClass,
                    startDestination = startDest
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainAppNavigation(
    viewModel: NutritionViewModel,
    widthSizeClass: WindowWidthSizeClass,
    startDestination: String = "dashboard"
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val showNavigation = currentRoute in listOf("dashboard", "summary", "ai_coach", "paywall")

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail for expanded/unfolded screen sizes
        if (showNavigation && widthSizeClass != WindowWidthSizeClass.Compact) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                header = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "AURA",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            ) {
                NavigationRailItem(
                    selected = currentRoute == "dashboard",
                    onClick = {
                        if (currentRoute != "dashboard") {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationRailItem(
                    selected = currentRoute == "summary",
                    onClick = {
                        if (currentRoute != "summary") {
                            navController.navigate("summary") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Trends") },
                    label = { Text("Trends") }
                )
                NavigationRailItem(
                    selected = currentRoute == "ai_coach",
                    onClick = {
                        if (currentRoute != "ai_coach") {
                            navController.navigate("ai_coach") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach") },
                    label = { Text("AI Coach") }
                )
                NavigationRailItem(
                    selected = currentRoute == "paywall",
                    onClick = {
                        if (currentRoute != "paywall") {
                            navController.navigate("paywall") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Premium") },
                    label = { Text("Premium") }
                )

                Spacer(modifier = Modifier.weight(1f))

                FloatingActionButton(
                    onClick = { navController.navigate("log_food/BREAKFAST") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera Scanner")
                }
            }
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            bottomBar = {
                // Bottom Bar for Compact screen sizes (Phones)
                if (showNavigation && widthSizeClass == WindowWidthSizeClass.Compact) {
                    BottomAppBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actions = {
                            IconButton(
                                onClick = {
                                    if (currentRoute != "dashboard") {
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = "Dashboard",
                                    tint = if (currentRoute == "dashboard") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (currentRoute != "summary") {
                                        navController.navigate("summary") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Trends",
                                    tint = if (currentRoute == "summary") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    if (currentRoute != "ai_coach") {
                                        navController.navigate("ai_coach") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Coach",
                                    tint = if (currentRoute == "ai_coach") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (currentRoute != "paywall") {
                                        navController.navigate("paywall") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Premium",
                                    tint = if (currentRoute == "paywall") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = { navController.navigate("log_food/BREAKFAST") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                                modifier = Modifier.offset(y = (-4).dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera Scanner")
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            navController = navController,
                            viewModel = viewModel,
                            widthSizeClass = widthSizeClass,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }

                    composable("paywall") {
                        PaywallScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }

                    composable("summary") {
                        WeeklySummaryScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }

                    composable("ai_coach") {
                        AICoachScreen(
                            navController = navController,
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }

                    composable(
                        route = "food_detail/{entryId}",
                        arguments = listOf(navArgument("entryId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val entryIdString = backStackEntry.arguments?.getString("entryId") ?: ""
                        val entryId = entryIdString.toIntOrNull() ?: 0
                        FoodDetailScreen(
                            navController = navController,
                            viewModel = viewModel,
                            entryId = entryId,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@composable
                        )
                    }

                    composable(
                        route = "log_food/{mealType}",
                        arguments = listOf(navArgument("mealType") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val mealType = backStackEntry.arguments?.getString("mealType") ?: ""
                        LogFoodScreen(
                            navController = navController,
                            viewModel = viewModel,
                            mealTypeString = mealType
                        )
                    }

                    composable(
                        route = "barcode_scanner/{mealType}",
                        arguments = listOf(navArgument("mealType") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val mealType = backStackEntry.arguments?.getString("mealType") ?: ""
                        BarcodeScannerScreen(
                            navController = navController,
                            viewModel = viewModel,
                            mealTypeString = mealType
                        )
                    }

                    composable(
                        route = "manual_add/{mealType}",
                        arguments = listOf(navArgument("mealType") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val mealType = backStackEntry.arguments?.getString("mealType") ?: ""
                        ManualAddScreen(
                            navController = navController,
                            viewModel = viewModel,
                            mealTypeString = mealType
                        )
                    }
                }
            }
        }
    }
}

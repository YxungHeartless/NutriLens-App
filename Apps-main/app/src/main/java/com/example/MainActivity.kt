package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogFoodScreen
import com.example.ui.screens.BarcodeScannerScreen
import com.example.ui.screens.ManualAddScreen
import com.example.ui.screens.WeeklySummaryScreen
import com.example.ui.screens.PaywallScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NutritionViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
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
                MainAppNavigation(viewModel = viewModel, startDestination = startDest)
            }
        }
    }
}

@Composable
fun MainAppNavigation(viewModel: NutritionViewModel, startDestination: String = "dashboard") {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                viewModel = viewModel
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

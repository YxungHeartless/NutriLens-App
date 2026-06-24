package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AICoachScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LogScreen
import com.example.ui.screens.RadarScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen() }
            composable("ai_coach") { AICoachScreen() }
            composable("scan_log") { LogScreen() }
            composable("radar") { RadarScreen() }
            composable("trends") { TrendsScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = { navController.navigate("dashboard") { launchSingleTop = true; restoreState = true; popUpTo("dashboard") { saveState = true } } },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = currentRoute == "ai_coach",
            onClick = { navController.navigate("ai_coach") { launchSingleTop = true; restoreState = true; popUpTo("dashboard") { saveState = true } } },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Coach") },
            label = { Text("AI Coach") }
        )
        NavigationBarItem(
            selected = currentRoute == "scan_log",
            onClick = { navController.navigate("scan_log") { launchSingleTop = true; restoreState = true; popUpTo("dashboard") { saveState = true } } },
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Scan & Log") },
            label = { Text("Scan & Log") }
        )
        NavigationBarItem(
            selected = currentRoute == "radar",
            onClick = { navController.navigate("radar") { launchSingleTop = true; restoreState = true; popUpTo("dashboard") { saveState = true } } },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Radar") },
            label = { Text("Radar") }
        )
        NavigationBarItem(
            selected = currentRoute == "trends",
            onClick = { navController.navigate("trends") { launchSingleTop = true; restoreState = true; popUpTo("dashboard") { saveState = true } } },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Trends") },
            label = { Text("Trends") }
        )
    }
}

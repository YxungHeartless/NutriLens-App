package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.data.billing.PremiumManager
import com.example.data.database.SubscriptionTier

@Composable
fun LoginScreen(
    navController: NavController
) {
    LaunchedEffect(Unit) {
        // Hardcoded bypass: Instantly inject "Verified Premium" session
        val premiumManager = PremiumManager.getInstance()
        premiumManager.isSandboxModeEnabled.value = true
        premiumManager.updateSubscriptionTier(SubscriptionTier.ULTRA)
        
        // Navigate instantly to dashboard, no login screens shown
        navController.navigate("dashboard") {
            popUpTo("login") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

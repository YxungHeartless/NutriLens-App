package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

enum class SplashStep {
    WARNING, ARTWORK
}

@Composable
fun SplashScreen(
    navController: NavController,
    authRepository: AuthRepository = koinInject()
) {
    var currentStep by remember { mutableStateOf(SplashStep.WARNING) }

    LaunchedEffect(key1 = Unit) {
        // Step 1: Warning holds for 1.5 seconds
        delay(1500)
        currentStep = SplashStep.ARTWORK
        
        // Step 2: Character Artwork holds for 2 seconds
        delay(2000)
        
        // Determine authentication status
        var nextDestination = "login"
        try {
            val currentUser = authRepository.currentUser.first()
            if (currentUser != null) {
                nextDestination = "dashboard"
            }
        } catch (e: Exception) {
            // Default to login if database or auth query fails
        }
        
        navController.navigate(nextDestination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep slate background
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = currentStep,
            animationSpec = tween(durationMillis = 600),
            label = "SplashTransition"
        ) { step ->
            when (step) {
                SplashStep.WARNING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Beta Warning",
                            tint = Color(0xFFEAB308), // Soft yellow warning color
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Beta Working Progress",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This app is a Beta working progress. Core visual engines, macro calculations, and location tracking APIs are continuously calibrating.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
                SplashStep.ARTWORK -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Custom premium character artwork container (glowing sphere representation)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            // Character artwork visual representation
                            Surface(
                                modifier = Modifier.size(96.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Info, // custom artwork icon
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            text = "HEARTLESS loading...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LinearProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier
                                .width(120.dp)
                                .height(4.dp)
                        )
                    }
                }
            }
        }
    }
}

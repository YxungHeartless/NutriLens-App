package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.database.SubscriptionTier
import com.example.ui.viewmodel.NutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    navController: NavController,
    viewModel: NutritionViewModel,
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val scrollState = rememberScrollState()
    
    // Backwards compatible premium state or sandbox toggle
    val isPremiumActive by viewModel.isPremiumUserFlow.collectAsState()
    var selectedTier by remember { mutableStateOf(SubscriptionTier.PRO) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Choose Your Tier",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("paywall_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("paywall_screen_container")
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Area
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "Premium Offer Icon",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(16.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Unlock NutriLens Premium",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("paywall_title")
                    )
                    Text(
                        text = "Select the tier that matches your wellness journey",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Sandbox Toggle Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Sandbox Sandbox Status",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPremiumActive) "PRO/ULTRA ACTIVE" else "FREE TIER ACTIVE",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isPremiumActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("premium_status")
                            )
                        }
                        IconButton(
                            onClick = { viewModel.togglePremiumStatus() },
                            modifier = Modifier.testTag("toggle_sandbox_premium")
                        ) {
                            Icon(
                                imageVector = if (isPremiumActive) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                                contentDescription = "Toggle subscription sandbox mode",
                                tint = if (isPremiumActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Adaptive pricing layouts
                if (widthSizeClass == WindowWidthSizeClass.Compact) {
                    // Vertical Stack for phones
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PricingCard(
                            tier = SubscriptionTier.FREE,
                            title = "Free Tier",
                            price = "$0.00",
                            highlights = listOf("Basic macro records", "Daily logging tracker", "Standard view logs"),
                            isSelected = selectedTier == SubscriptionTier.FREE,
                            onSelect = { selectedTier = SubscriptionTier.FREE }
                        )
                        PricingCard(
                            tier = SubscriptionTier.PRO,
                            title = "Pro Tier",
                            price = "$4.99",
                            subtitle = "Geo-shopping & Radar",
                            highlights = listOf("Interactive location mapping", "Macro radar tags", "7-day trends & charts"),
                            isSelected = selectedTier == SubscriptionTier.PRO,
                            onSelect = { selectedTier = SubscriptionTier.PRO }
                        )
                        PricingCard(
                            tier = SubscriptionTier.ULTRA,
                            title = "Ultra Tier",
                            price = "$9.99",
                            subtitle = "AI Fridge Scanner & Recipes",
                            highlights = listOf("Unlimited AI Fridge Scans", "Automatic recipe macro builders", "Interactive AI Coach chats"),
                            isSelected = selectedTier == SubscriptionTier.ULTRA,
                            onSelect = { selectedTier = SubscriptionTier.ULTRA }
                        )
                    }
                } else {
                    // Horizontal Grid for tablets/foldables
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PricingCard(
                                tier = SubscriptionTier.FREE,
                                title = "Free Tier",
                                price = "$0.00",
                                highlights = listOf("Basic macro records", "Daily logging tracker", "Standard view logs"),
                                isSelected = selectedTier == SubscriptionTier.FREE,
                                onSelect = { selectedTier = SubscriptionTier.FREE }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PricingCard(
                                tier = SubscriptionTier.PRO,
                                title = "Pro Tier",
                                price = "$4.99",
                                subtitle = "Geo-shopping & Radar",
                                highlights = listOf("Interactive location mapping", "Macro radar tags", "7-day trends & charts"),
                                isSelected = selectedTier == SubscriptionTier.PRO,
                                onSelect = { selectedTier = SubscriptionTier.PRO }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PricingCard(
                                tier = SubscriptionTier.ULTRA,
                                title = "Ultra Tier",
                                price = "$9.99",
                                subtitle = "AI Fridge Scanner & Recipes",
                                highlights = listOf("Unlimited AI Fridge Scans", "Automatic recipe macro builders", "Interactive AI Coach chats"),
                                isSelected = selectedTier == SubscriptionTier.ULTRA,
                                onSelect = { selectedTier = SubscriptionTier.ULTRA }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start 7-Day Trial Call-to-action button
                Button(
                    onClick = {
                        viewModel.selectSubscriptionTier(selectedTier)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("subscribe_now_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Start 7-Day Free Trial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun PricingCard(
    tier: SubscriptionTier,
    title: String,
    price: String,
    subtitle: String = "",
    highlights: List<String>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val borderWidth = if (isSelected) 2.5.dp else 1.dp
    
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (tier == SubscriptionTier.ULTRA) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "ULTIMATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/ month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                highlights.forEach { highlight ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = highlight,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

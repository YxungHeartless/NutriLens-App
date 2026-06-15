package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.model.FoodEntry
import com.example.ui.components.SlateBg
import com.example.ui.components.CardDark
import com.example.ui.components.SoftLime
import com.example.ui.components.GrayText
import com.example.ui.components.ProteinOrange
import com.example.ui.components.CarbYellow
import com.example.ui.components.FatBlue
import com.example.ui.viewmodel.NutritionViewModel

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    navController: NavController,
    viewModel: NutritionViewModel,
    entryId: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val entries by viewModel.allLoggedMeals.collectAsState()
    val entry = entries.find { it.id == entryId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Log Details", color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateBg)
            )
        },
        containerColor = SlateBg
    ) { paddingValues ->
        if (entry != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                with(sharedTransitionScope) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .sharedElement(
                                rememberSharedContentState(key = "food_card_${entry.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        FoodDetailContent(
                            entry = entry,
                            onDelete = {
                                viewModel.deleteEntry(entry)
                                navController.popBackStack()
                            },
                            modifier = Modifier.padding(24.dp),
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Log entry not found.", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FoodDetailContent(
    entry: FoodEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Food Title & Meal Type Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Text(
                            text = entry.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White,
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "food_name_${entry.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        )
                    }
                } else {
                    Text(
                        text = entry.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${entry.mealType.lowercase().replaceFirstChar { it.uppercase() }} • ${entry.servingSize} ${entry.servingUnit}",
                    color = GrayText,
                    fontSize = 13.sp
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

        // Location Tag Card
        if (entry.locationName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SlateBg),
                border = BorderStroke(1.dp, SoftLime.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(SoftLime.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Tag",
                            tint = SoftLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Logged Location",
                            fontSize = 11.sp,
                            color = GrayText
                        )
                        Text(
                            text = entry.locationName,
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Calories Banner Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estimated Energy",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${entry.calories.toInt()} kcal",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Macro Progress Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateBg.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Macronutrients Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )

                MacroDetailRow(
                    label = "Protein",
                    amount = entry.protein,
                    color = ProteinOrange,
                    percentage = (entry.protein * 4 / (entry.calories.coerceAtLeast(1.0)) * 100).toInt()
                )

                MacroDetailRow(
                    label = "Carbohydrates",
                    amount = entry.carbs,
                    color = CarbYellow,
                    percentage = (entry.carbs * 4 / (entry.calories.coerceAtLeast(1.0)) * 100).toInt()
                )

                MacroDetailRow(
                    label = "Fats",
                    amount = entry.fats,
                    color = FatBlue,
                    percentage = (entry.fats * 9 / (entry.calories.coerceAtLeast(1.0)) * 100).toInt()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Delete Button
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Log",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Food Log Entry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun MacroDetailRow(
    label: String,
    amount: Double,
    color: Color,
    percentage: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "${amount.toInt()}g (${percentage}%)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (amount * 4 / 300.0).coerceIn(0.0, 1.0).toFloat() }, // Show relative to standard meal size
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.04f)
        )
    }
}

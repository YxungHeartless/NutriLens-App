package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.domain.model.MealType
import com.example.ui.components.*
import com.example.ui.viewmodel.NutritionViewModel

data class FoodPreset(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val servingSize: Double,
    val servingUnit: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddScreen(
    navController: NavController,
    viewModel: NutritionViewModel,
    mealTypeString: String
) {
    val mealType = MealType.fromString(mealTypeString)
    val suggestions by viewModel.manualSuggestions.collectAsState()
    val isSearching by viewModel.isSearchingSuggestions.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSuggestions()
        }
    }

    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("1.0") }
    var servingUnit by remember { mutableStateOf("serving") }

    val presets = listOf(
        FoodPreset("Fresh Banana 🍌", 105.0, 1.3, 27.0, 0.3, 1.0, "medium"),
        FoodPreset("Large Boiled Egg 🥚", 78.0, 6.3, 0.6, 5.3, 1.0, "large"),
        FoodPreset("Grilled Chicken Breast 🍗", 165.0, 31.0, 0.0, 3.6, 100.0, "g"),
        FoodPreset("Brown Jasmine Rice 🍚", 215.0, 5.0, 45.0, 1.6, 1.0, "cup"),
        FoodPreset("Creamy Peanut Butter 🥜", 94.0, 4.0, 3.0, 8.0, 1.0, "tbsp"),
        FoodPreset("Whey Protein Shake 🥤", 120.0, 24.0, 3.0, 1.5, 1.0, "scoop")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual Log Entry (${mealType.displayName})", color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateBg)
            )
        },
        containerColor = SlateBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset suggestion chips header
            Text(
                text = "Dynamic ingredient presets",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            // Scrollable preset suggestion chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                presets.forEach { pr ->
                    InputChip(
                        selected = false,
                        onClick = {
                            foodName = pr.name
                            calories = pr.calories.toInt().toString()
                            protein = pr.protein.toInt().toString()
                            carbs = pr.carbs.toInt().toString()
                            fats = pr.fats.toInt().toString()
                            servingSize = pr.servingSize.toString()
                            servingUnit = pr.servingUnit
                        },
                        label = { Text(pr.name, color = Color.White, fontSize = 12.sp) },
                        colors = InputChipDefaults.inputChipColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.testTag("preset_${pr.name.lowercase().replace(" ", "_")}")
                    )
                }
            }

            // Central Food Item Inputs Form Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = {
                                foodName = it
                                viewModel.searchFoodSuggestions(it)
                            },
                            label = { Text("Food Item Name", color = SoftLime) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            trailingIcon = {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = SoftLime
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manual_food_name_input")
                        )
                    }

                    if (suggestions.isNotEmpty() && foodName.length >= 2) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .geminiGlass(cornerRadius = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Column {
                                suggestions.forEach { food ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                foodName = food.description
                                                
                                                // Extract macros
                                                var cal = 0.0
                                                var prot = 0.0
                                                var carb = 0.0
                                                var fat = 0.0
                                                food.foodNutrients?.forEach { nutrient ->
                                                    val name = nutrient.nutrientName ?: ""
                                                    val id = nutrient.nutrientId
                                                    val valD = nutrient.value ?: 0.0
                                                    
                                                    when {
                                                        name.contains("protein", ignoreCase = true) || id == 1003 -> prot = valD
                                                        (name.contains("fat", ignoreCase = true) || name.contains("lipid", ignoreCase = true)) && !name.contains("fatty", ignoreCase = true) || id == 1004 -> fat = valD
                                                        name.contains("carbohydrate", ignoreCase = true) || id == 1005 -> carb = valD
                                                        (name.contains("energy", ignoreCase = true) && (nutrient.unitName?.contains("kcal", ignoreCase = true) == true || nutrient.unitName?.contains("energy", ignoreCase = true) == true)) || id == 1008 -> cal = valD
                                                    }
                                                }
                                                calories = cal.toInt().toString()
                                                protein = prot.toInt().toString()
                                                carbs = carb.toInt().toString()
                                                fats = fat.toInt().toString()
                                                servingSize = "1.0"
                                                servingUnit = "serving"
                                                
                                                // Clear suggestions after selection
                                                viewModel.clearSuggestions()
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = food.description,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            val cal = food.foodNutrients?.find { (it.nutrientName ?: "").contains("energy", ignoreCase = true) || it.nutrientId == 1008 }?.value ?: 0.0
                                            Text(
                                                text = "USDA ID: ${food.fdcId} • ${cal.toInt()} kcal per 100g",
                                                color = SoftLime,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Select suggestion",
                                            tint = SoftLime,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Calories (kcal)", color = SoftLime) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_calories_input")
                        )

                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Protein (g)", color = SoftLime) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_protein_input")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = carbs,
                            onValueChange = { carbs = it },
                            label = { Text("Carbohydrates (g)", color = SoftLime) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_carbs_input")
                        )

                        OutlinedTextField(
                            value = fats,
                            onValueChange = { fats = it },
                            label = { Text("Fats (g)", color = SoftLime) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_fats_input")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = servingSize,
                            onValueChange = { servingSize = it },
                            label = { Text("Serving Size", color = SoftLime) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_serving_size_input")
                        )

                        OutlinedTextField(
                            value = servingUnit,
                            onValueChange = { servingUnit = it },
                            label = { Text("Serving Unit", color = SoftLime) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftLime,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_serving_unit_input")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ingestion submission trigger action
            Button(
                onClick = {
                    if (foodName.isNotBlank() && calories.isNotBlank()) {
                        viewModel.logFood(
                            name = foodName,
                            calories = calories.toDoubleOrNull() ?: 0.0,
                            protein = protein.toDoubleOrNull() ?: 0.0,
                            carbs = carbs.toDoubleOrNull() ?: 0.0,
                            fats = fats.toDoubleOrNull() ?: 0.0,
                            mealType = mealType,
                            servingSize = servingSize.toDoubleOrNull() ?: 1.0,
                            servingUnit = servingUnit.ifBlank { "serving" }
                        )

                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    }
                },
                enabled = foodName.isNotBlank() && calories.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftLime,
                    disabledContainerColor = Color.White.copy(alpha = 0.06f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_manual_log_button")
            ) {
                Text("Log Food Item", color = SlateBg, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

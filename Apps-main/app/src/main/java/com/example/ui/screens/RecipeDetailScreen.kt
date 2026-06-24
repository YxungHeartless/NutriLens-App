package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.example.data.database.RecipeCatalogEntity
import com.example.ui.components.CardDark
import com.example.ui.components.SlateBg
import com.example.ui.components.SoftLime
import com.example.ui.components.GrayText
import com.example.ui.viewmodel.NutritionViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    viewModel: NutritionViewModel,
    recipeId: Int
) {
    var recipeEntity by remember { mutableStateOf<RecipeCatalogEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(recipeId) {
        isLoading = true
        recipeEntity = viewModel.getRecipeById(recipeId)
        isLoading = false
    }

    // Window Layout / Fold Tracking
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var displayFeatures by remember { mutableStateOf<List<androidx.window.layout.DisplayFeature>>(emptyList()) }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            WindowInfoTracker.getOrCreate(context)
                .windowLayoutInfo(context)
                .collect { newLayoutInfo ->
                    displayFeatures = newLayoutInfo.displayFeatures
                }
        }
    }

    val foldingFeature = displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
    val isTabletop = foldingFeature != null && foldingFeature.state == FoldingFeature.State.HALF_OPENED

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipeEntity?.title ?: "Recipe Companion", color = Color.White, fontSize = 16.sp) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = SoftLime,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val recipe = recipeEntity
                if (recipe == null) {
                    Text(
                        text = "Recipe not found",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    val moshi = remember { Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build() }
                    val listAdapter = remember {
                        moshi.adapter<List<String>>(
                            com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
                        )
                    }
                    val ingredientsList = remember(recipe) {
                        try {
                            listAdapter.fromJson(recipe.ingredientsJson) ?: emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    val instructionsList = remember(recipe) {
                        recipe.instructions.split("\n").filter { it.isNotBlank() }
                    }

                    val checkedIngredients = remember { mutableStateListOf<Boolean>() }
                    LaunchedEffect(ingredientsList) {
                        checkedIngredients.clear()
                        ingredientsList.forEach { _ -> checkedIngredients.add(false) }
                    }

                    var activeStepIndex by remember { mutableStateOf(0) }

                    if (isTabletop) {
                        // HALF_OPENED Foldable Layout: split step vs ingredients & next button
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SlateBg)
                        ) {
                            // Top Half: Cooking Step Instruction
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(CardDark)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "STEP ${activeStepIndex + 1} OF ${instructionsList.size}",
                                        color = SoftLime,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = instructionsList.getOrNull(activeStepIndex) ?: "Ready to eat!",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 32.sp
                                    )
                                }
                            }

                            // Bottom Half: Ingredients checklist & Next Button
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Ingredients Checklist:",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ingredientsList.forEachIndexed { index, ingredient ->
                                        val isChecked = checkedIngredients.getOrElse(index) { false }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (index < checkedIngredients.size) {
                                                        checkedIngredients[index] = !isChecked
                                                    }
                                                }
                                                .padding(vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    if (index < checkedIngredients.size) {
                                                        checkedIngredients[index] = checked ?: false
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = SoftLime,
                                                    uncheckedColor = Color.White.copy(alpha = 0.3f),
                                                    checkmarkColor = SlateBg
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = ingredient,
                                                color = if (isChecked) Color.White.copy(alpha = 0.4f) else Color.White,
                                                fontSize = 13.sp,
                                                textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        if (activeStepIndex < instructionsList.size - 1) {
                                            activeStepIndex++
                                        } else {
                                            navController.popBackStack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftLime),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(68.dp), // Massive touch target
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = if (activeStepIndex < instructionsList.size - 1) "Next Step ➔" else "Finish Cooking 🎉",
                                        color = SlateBg,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard flat scrolling layout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SlateBg)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(recipe.title, color = SoftLime, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    val macrosMap = remember(recipe) {
                                        try {
                                            val mapAdapter = moshi.adapter<Map<String, Double>>(
                                                com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType)
                                            )
                                            mapAdapter.fromJson(recipe.aiGeneratedMacros) ?: emptyMap()
                                        } catch (e: Exception) {
                                            emptyMap()
                                        }
                                    }
                                    val calories = macrosMap["calories"]?.toInt() ?: 0
                                    val protein = macrosMap["protein"]?.toInt() ?: 0
                                    val carbs = macrosMap["carbs"]?.toInt() ?: 0
                                    val fat = macrosMap["fat"]?.toInt() ?: 0

                                    Text("🔥 Calories: $calories kcal", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                    Text("💪 Protein: ${protein}g | 🍞 Carbs: ${carbs}g | 🥑 Fat: ${fat}g", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Ingredients Checklist:", color = SoftLime, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    ingredientsList.forEachIndexed { index, ingredient ->
                                        val isChecked = checkedIngredients.getOrElse(index) { false }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (index < checkedIngredients.size) {
                                                        checkedIngredients[index] = !isChecked
                                                    }
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    if (index < checkedIngredients.size) {
                                                        checkedIngredients[index] = checked ?: false
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = SoftLime,
                                                    checkmarkColor = SlateBg
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = ingredient,
                                                color = if (isChecked) Color.White.copy(alpha = 0.4f) else Color.White,
                                                fontSize = 13.sp,
                                                textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                            )
                                        }
                                    }
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text("Instructions Step-by-Step:", color = SoftLime, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    instructionsList.forEachIndexed { i, step ->
                                        Row(modifier = Modifier.padding(vertical = 6.dp)) {
                                            Text("${i + 1}. ", color = SoftLime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(step, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

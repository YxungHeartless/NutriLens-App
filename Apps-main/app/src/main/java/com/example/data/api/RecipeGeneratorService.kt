package com.example.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GeneratedRecipe(
    val title: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

class RecipeGeneratorService {

    suspend fun generateRecipe(ingredientsList: List<String>, customInput: String? = null): GeneratedRecipe = withContext(Dispatchers.IO) {
        // Fallback mock recipe
        return@withContext GeneratedRecipe(
            title = "High-Protein Harvest Bowl",
            ingredients = listOf("150g Chicken Breast", "100g Sweet Potato", "1 cup Broccoli florets"),
            instructions = listOf(
                "Bake sweet potato cubes at 400°F (200°C) for 25 minutes.",
                "Grill chicken breast for 6 minutes each side until cooked through.",
                "Steam broccoli for 4 minutes.",
                "Assemble in a bowl and serve."
            ),
            calories = 380.0,
            protein = 42.0,
            carbs = 28.0,
            fat = 8.0
        )
    }
}

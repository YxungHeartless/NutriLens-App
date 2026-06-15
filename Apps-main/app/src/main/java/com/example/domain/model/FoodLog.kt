package com.example.domain.model

data class FoodLog(
    val id: Int = 0,
    val name: String,
    val calories: Double, // kcal
    val protein: Double,  // grams
    val carbs: Double,    // grams
    val fats: Double,     // grams
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: MealType,
    val imagePath: String? = null,
    val barcode: String? = null,
    val servingSize: Double = 1.0,
    val servingUnit: String = "serving",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null
)

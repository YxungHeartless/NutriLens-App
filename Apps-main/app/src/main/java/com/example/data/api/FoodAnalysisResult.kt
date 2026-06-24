package com.example.data.api

data class FoodAnalysisResult(
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val servingSize: Double,
    val servingUnit: String,
    val confidenceScore: Double? = 1.0,
    val description: String? = null
)

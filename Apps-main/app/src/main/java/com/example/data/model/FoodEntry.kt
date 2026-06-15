package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val mealType: String, // Maps to MealType.name
    val servingSize: Double,
    val servingUnit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val barcode: String? = null,
    val isSynced: Boolean = true
)

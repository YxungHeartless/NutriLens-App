package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val foodItemId: Int,
    val portionSize: Double,
    val mealType: String // "Breakfast", "Lunch", "Dinner", "Snack"
)

package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.FoodLog
import com.example.domain.model.MealType

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val timestamp: Long,
    val mealType: String, // Store enum name
    val imagePath: String?,
    val barcode: String?,
    val servingSize: Double,
    val servingUnit: String,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?
) {
    fun toDomain(): FoodLog {
        val mealEnum = try {
            MealType.valueOf(mealType)
        } catch (e: Exception) {
            MealType.SNACK
        }
        return FoodLog(
            id = id,
            name = name,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats,
            timestamp = timestamp,
            mealType = mealEnum,
            imagePath = imagePath,
            barcode = barcode,
            servingSize = servingSize,
            servingUnit = servingUnit,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName
        )
    }

    companion object {
        fun fromDomain(domain: FoodLog): FoodLogEntity {
            return FoodLogEntity(
                id = domain.id,
                name = domain.name,
                calories = domain.calories,
                protein = domain.protein,
                carbs = domain.carbs,
                fats = domain.fats,
                timestamp = domain.timestamp,
                mealType = domain.mealType.name,
                imagePath = domain.imagePath,
                barcode = domain.barcode,
                servingSize = domain.servingSize,
                servingUnit = domain.servingUnit,
                latitude = domain.latitude,
                longitude = domain.longitude,
                locationName = domain.locationName
            )
        }
    }
}

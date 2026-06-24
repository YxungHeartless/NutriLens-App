package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items")
    fun getAllFoodItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    fun getFoodItemById(id: Int): Flow<FoodItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItemEntity): Long

    @Update
    suspend fun updateFoodItem(item: FoodItemEntity)

    @Delete
    suspend fun deleteFoodItem(item: FoodItemEntity)
}

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC")
    fun getAllMealLogs(): Flow<List<MealLogEntity>>

    @Query("""
        SELECT ml.id, ml.timestamp, ml.foodItemId, ml.portionSize, ml.mealType,
               fi.name AS foodName, fi.calories, fi.protein, fi.carbs, fi.fat
        FROM meal_logs ml
        INNER JOIN food_items fi ON ml.foodItemId = fi.id
        WHERE ml.timestamp >= :startTime AND ml.timestamp < :endTime
    """)
    fun getMealLogsWithFoodForTimeRange(startTime: Long, endTime: Long): Flow<List<MealLogWithFood>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(log: MealLogEntity): Long

    @Update
    suspend fun updateMealLog(log: MealLogEntity)

    @Delete
    suspend fun deleteMealLog(log: MealLogEntity)
}

data class MealLogWithFood(
    val id: Int,
    val timestamp: Long,
    val foodItemId: Int,
    val portionSize: Double,
    val mealType: String,
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Dao
interface RecipeCatalogDao {
    @Query("SELECT * FROM recipe_catalog")
    fun getAllRecipes(): Flow<List<RecipeCatalogEntity>>

    @Query("SELECT * FROM recipe_catalog WHERE id = :id")
    suspend fun getRecipeById(id: Int): RecipeCatalogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeCatalogEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeCatalogEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeCatalogEntity)
}

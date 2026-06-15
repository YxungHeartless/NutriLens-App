package com.example.data.local

import androidx.room.*
import com.example.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries ORDER BY timestamp DESC")
    fun getAllEntriesFlow(): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getEntriesForDayFlow(startOfDay: Long, endOfDay: Long): Flow<List<FoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FoodEntry)

    @Delete
    suspend fun deleteEntry(entry: FoodEntry)

    @Query("DELETE FROM food_entries")
    suspend fun clearAllEntries()

    @Query("SELECT * FROM food_entries WHERE isSynced = 0")
    suspend fun getUnsyncedEntries(): List<FoodEntry>

    @Query("UPDATE food_entries SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllSynced()
}

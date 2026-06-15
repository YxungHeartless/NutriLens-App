package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<FoodLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FoodLogEntity): Long

    @Update
    suspend fun updateLog(log: FoodLogEntity)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("SELECT * FROM food_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Int): FoodLogEntity?
}

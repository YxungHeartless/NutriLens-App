package com.example.domain.repository

import com.example.domain.model.FoodLog
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllLogs(): Flow<List<FoodLog>>
    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<FoodLog>>
    suspend fun insertLog(log: FoodLog): Long
    suspend fun updateLog(log: FoodLog)
    suspend fun deleteLog(id: Int)
    suspend fun getLogById(id: Int): FoodLog?
}

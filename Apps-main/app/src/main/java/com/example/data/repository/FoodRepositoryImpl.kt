package com.example.data.repository

import com.example.data.database.FoodLogDao
import com.example.data.database.FoodLogEntity
import com.example.domain.model.FoodLog
import com.example.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FoodRepositoryImpl(private val foodLogDao: FoodLogDao) : FoodRepository {
    override fun getAllLogs(): Flow<List<FoodLog>> {
        return foodLogDao.getAllLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<FoodLog>> {
        return foodLogDao.getLogsBetween(startTime, endTime).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertLog(log: FoodLog): Long {
        return foodLogDao.insertLog(FoodLogEntity.fromDomain(log))
    }

    override suspend fun updateLog(log: FoodLog) {
        foodLogDao.updateLog(FoodLogEntity.fromDomain(log))
    }

    override suspend fun deleteLog(id: Int) {
        foodLogDao.deleteLogById(id)
    }

    override suspend fun getLogById(id: Int): FoodLog? {
        return foodLogDao.getLogById(id)?.toDomain()
    }
}

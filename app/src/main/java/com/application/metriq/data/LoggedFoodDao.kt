package com.application.metriq.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LoggedFoodDao {
    @Insert
    suspend fun insert(food: LoggedFood)

    @Query("SELECT * FROM logged_foods WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getFoodsForDate(startOfDay: Long, endOfDay: Long): Flow<List<LoggedFood>>
}

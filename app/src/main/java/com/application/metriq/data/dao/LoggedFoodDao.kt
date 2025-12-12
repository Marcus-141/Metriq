package com.application.metriq.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.application.metriq.data.entity.LoggedFood
import kotlinx.coroutines.flow.Flow

@Dao
interface LoggedFoodDao {
    @Insert
    suspend fun insert(food: LoggedFood)

    @Delete
    suspend fun delete(food: LoggedFood)

    @Query("SELECT * FROM logged_foods WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getFoodsForDate(startOfDay: Long, endOfDay: Long): Flow<List<LoggedFood>>

    @Query("SELECT * FROM logged_foods ORDER BY timestamp DESC")
    fun getAllFoods(): Flow<List<LoggedFood>>
}

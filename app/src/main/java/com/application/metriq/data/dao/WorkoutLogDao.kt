package com.application.metriq.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.application.metriq.data.entity.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Insert
    suspend fun insert(log: WorkoutLog)

    @Delete
    suspend fun delete(log: WorkoutLog)

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<WorkoutLog>>
}

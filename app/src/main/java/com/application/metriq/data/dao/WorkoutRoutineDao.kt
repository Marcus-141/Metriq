package com.application.metriq.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.application.metriq.data.WorkoutRoutine

@Dao
interface WorkoutRoutineDao {
    @Insert
    suspend fun insert(workoutRoutine: WorkoutRoutine): Long

    @Update
    suspend fun update(workoutRoutine: WorkoutRoutine)

    @Delete
    suspend fun delete(workoutRoutine: WorkoutRoutine)

    @Query("SELECT * FROM workout_routines ORDER BY id DESC")
    fun getAllRoutines(): Flow<List<WorkoutRoutine>>
    
    @Query("SELECT * FROM workout_routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): WorkoutRoutine?
}

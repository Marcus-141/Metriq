package com.application.metriq.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.application.metriq.data.dao.LoggedFoodDao
import com.application.metriq.data.dao.WorkoutRoutineDao
import com.application.metriq.data.entity.LoggedFood
import com.application.metriq.data.entity.WorkoutRoutine

@Database(entities = [WorkoutRoutine::class, LoggedFood::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutRoutineDao(): WorkoutRoutineDao
    abstract fun loggedFoodDao(): LoggedFoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "metriq_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

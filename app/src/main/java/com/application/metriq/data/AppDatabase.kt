package com.application.metriq.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.application.metriq.data.dao.WorkoutRoutineDao
import com.application.metriq.data.entity.WorkoutRoutine

@Database(entities = [LoggedFood::class, WorkoutRoutine::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loggedFoodDao(): LoggedFoodDao
    abstract fun workoutRoutineDao(): WorkoutRoutineDao

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
                .fallbackToDestructiveMigration() // Handling migration simply for now
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

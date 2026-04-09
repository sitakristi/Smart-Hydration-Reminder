package com.rensy.smarthydration.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smarthydration.database.dao.DailyProgressDao
import com.example.smarthydration.database.dao.HydrationLogDao
import com.example.smarthydration.database.dao.UserDao
import com.example.smarthydration.model.DailyProgress
import com.example.smarthydration.model.HydrationLog
import com.example.smarthydration.model.User

@Database(
    entities = [User::class, HydrationLog::class, DailyProgress::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun hydrationLogDao(): HydrationLogDao
    abstract fun dailyProgressDao(): DailyProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_hydration.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
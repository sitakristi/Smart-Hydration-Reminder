package com.rensy.smarthydration.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rensy.smarthydration.database.dao.DailyProgressDao
import com.rensy.smarthydration.database.dao.HydrationLogDao
import com.rensy.smarthydration.database.dao.UserDao
import com.rensy.smarthydration.model.DailyProgressModel
import com.rensy.smarthydration.model.HydrationLogModel
import com.rensy.smarthydration.model.UserModel

@Database(
    entities = [UserModel::class, HydrationLogModel::class, DailyProgressModel::class],
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
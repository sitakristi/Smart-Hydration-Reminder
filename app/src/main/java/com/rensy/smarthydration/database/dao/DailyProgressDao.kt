package com.rensy.smarthydration.database.dao

import androidx.room.*
import com.rensy.smarthydration.model.DailyProgress

@Dao
interface DailyProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: DailyProgress): Long

    @Update
    suspend fun update(progress: DailyProgress)

    /**
     * Ambil progress harian untuk user pada tanggal tertentu (format "yyyy-MM-dd").
     * Karena ada UNIQUE(userID, date), hasilnya maksimal 1 record.
     */
    @Query("SELECT * FROM DAILY_PROGRESS WHERE userID = :userID AND date = :date LIMIT 1")
    suspend fun getProgressByDate(userID: Int, date: String): DailyProgress?

    /**
     * Ambil semua progress dalam rentang tanggal (untuk laporan).
     */
    @Query("SELECT * FROM DAILY_PROGRESS WHERE userID = :userID AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getProgressBetweenDates(userID: Int, startDate: String, endDate: String): List<DailyProgress>

    /**
     * Hitung jumlah hari target terpenuhi dalam rentang tanggal.
     */
    @Query("SELECT COUNT(*) FROM DAILY_PROGRESS WHERE userID = :userID AND date BETWEEN :startDate AND :endDate AND isAchieved = 1")
    suspend fun countAchievedDays(userID: Int, startDate: String, endDate: String): Int

    @Query("DELETE FROM DAILY_PROGRESS WHERE userID = :userID AND date = :date")
    suspend fun deleteByDate(userID: Int, date: String)
}
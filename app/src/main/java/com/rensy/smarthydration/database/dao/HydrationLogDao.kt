package com.rensy.smarthydration.database.dao

import androidx.room.*
import com.rensy.smarthydration.model.HydrationLog

@Dao
interface HydrationLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HydrationLog): Long

    @Delete
    suspend fun delete(log: HydrationLog)

    @Query("DELETE FROM HYDRATION_LOG WHERE logID = :logID")
    suspend fun deleteById(logID: Int)

    /**
     * Ambil semua log untuk user pada tanggal tertentu (format "yyyy-MM-dd").
     */
    @Query("SELECT * FROM HYDRATION_LOG WHERE userID = :userID AND logDate = :date ORDER BY timestamp ASC")
    suspend fun getLogsByDate(userID: Int, date: String): List<HydrationLog>

    /**
     * Ambil total intake hari ini untuk user tertentu.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM HYDRATION_LOG WHERE userID = :userID AND logDate = :date")
    suspend fun getTotalIntakeByDate(userID: Int, date: String): Int

    /**
     * Ambil semua log dalam rentang tanggal (untuk laporan bulanan).
     */
    @Query("SELECT * FROM HYDRATION_LOG WHERE userID = :userID AND logDate BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    suspend fun getLogsBetweenDates(userID: Int, startDate: String, endDate: String): List<HydrationLog>

    @Query("SELECT * FROM HYDRATION_LOG WHERE userID = :userID ORDER BY timestamp DESC")
    suspend fun getAllLogs(userID: Int): List<HydrationLog>
}
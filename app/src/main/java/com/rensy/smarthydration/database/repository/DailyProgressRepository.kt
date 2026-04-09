package com.rensy.smarthydration.database.repository

import com.rensy.smarthydration.database.dao.DailyProgressDao
import com.rensy.smarthydration.model.DailyProgress
import java.text.SimpleDateFormat
import java.util.*

class DailyProgressRepository(private val dailyProgressDao: DailyProgressDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Mengambil progress harian hari ini untuk user tertentu.
     * Jika belum ada record, return null — caller harus membuat record baru.
     */
    suspend fun getTodayProgress(userID: Int): DailyProgress? {
        val today = dateFormat.format(Date())
        return dailyProgressDao.getProgressByDate(userID, today)
    }

    /**
     * Mengambil progress pada tanggal tertentu.
     */
    suspend fun getProgressByDate(userID: Int, date: String): DailyProgress? {
        return dailyProgressDao.getProgressByDate(userID, date)
    }

    /**
     * Menyimpan atau memperbarui progress harian.
     * Jika record untuk (userID, date) sudah ada, akan di-replace (UNIQUE constraint).
     */
    suspend fun saveOrUpdateProgress(progress: DailyProgress) {
        dailyProgressDao.insert(progress)
    }

    /**
     * Memperbarui total intake dan recalculate percentage, remaining, isAchieved.
     * Jika belum ada record hari ini, buat baru dengan targetAmount dari parameter.
     */
    suspend fun updateTodayProgress(userID: Int, newTotalIntake: Int, targetAmount: Int) {
        val today = dateFormat.format(Date())
        val existing = dailyProgressDao.getProgressByDate(userID, today)
        val updated = if (existing != null) {
            existing.withUpdatedIntake(newTotalIntake)
        } else {
            DailyProgress(
                userID = userID,
                date = today,
                totalIntake = newTotalIntake,
                targetAmount = targetAmount
            ).withUpdatedIntake(newTotalIntake)
        }
        dailyProgressDao.insert(updated)
    }

    /**
     * Mengambil progress dalam rentang tanggal (untuk laporan).
     */
    suspend fun getProgressBetweenDates(userID: Int, startDate: String, endDate: String): List<DailyProgress> {
        return dailyProgressDao.getProgressBetweenDates(userID, startDate, endDate)
    }

    /**
     * Menghitung jumlah hari target terpenuhi dalam rentang tanggal.
     */
    suspend fun countAchievedDays(userID: Int, startDate: String, endDate: String): Int {
        return dailyProgressDao.countAchievedDays(userID, startDate, endDate)
    }
}
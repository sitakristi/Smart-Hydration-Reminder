package com.rensy.smarthydration.database.repository

import com.rensy.smarthydration.database.dao.DailyProgressDao
import com.rensy.smarthydration.model.DailyProgress
import java.text.SimpleDateFormat
import java.util.*

class DailyProgressRepository(private val dailyProgressDao: DailyProgressDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun getTodayProgress(userID: Int): DailyProgress? {
        val today = dateFormat.format(Date())
        return dailyProgressDao.getProgressByDate(userID, today)
    }

    suspend fun getProgressByDate(userID: Int, date: String): DailyProgress? {
        return dailyProgressDao.getProgressByDate(userID, date)
    }

    suspend fun saveOrUpdateProgress(progress: DailyProgress) {
        dailyProgressDao.insert(progress)
    }

    suspend fun updateTodayProgress(userID: Int, newTotalIntake: Int, targetAmount: Int) {
        val today = dateFormat.format(Date())
        val existing = dailyProgressDao.getProgressByDate(userID, today)
        val updated = if (existing != null) {
            existing.withUpdatedIntake(newTotalIntake)
        } else {
            DailyProgress(
                userID = userID,
                date = today,
                totalIntake = 0,
                targetAmount = targetAmount,
                // pakai percentageValue (bukan percentage)
                percentageValue = 0f,
                remainingAmount = targetAmount,
                isAchieved = false
            ).withUpdatedIntake(newTotalIntake)
        }
        dailyProgressDao.insert(updated)
    }

    suspend fun getProgressBetweenDates(userID: Int, startDate: String, endDate: String): List<DailyProgress> {
        return dailyProgressDao.getProgressBetweenDates(userID, startDate, endDate)
    }

    suspend fun countAchievedDays(userID: Int, startDate: String, endDate: String): Int {
        return dailyProgressDao.countAchievedDays(userID, startDate, endDate)
    }
}
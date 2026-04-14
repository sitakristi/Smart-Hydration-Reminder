package com.rensy.smarthydration.database.repository

import com.rensy.smarthydration.database.dao.HydrationLogDao
import com.rensy.smarthydration.model.HydrationLogModel
import java.text.SimpleDateFormat
import java.util.*

class HydrationLogRepository(private val hydrationLogDao: HydrationLogDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Menyimpan entri konsumsi air baru ke database.
     * logDate di-set otomatis berdasarkan timestamp saat ini.
     * @return logID yang di-generate database
     */
    suspend fun saveLog(userID: Int, amount: Int): Long {
        val now = System.currentTimeMillis()
        val log = HydrationLogModel(
            userID = userID,
            amount = amount,
            timestamp = now,
            logDate = dateFormat.format(Date(now))
        )
        return hydrationLogDao.insert(log)
    }

    /**
     * Menghapus entri log berdasarkan logID.
     */
    suspend fun deleteLog(logID: Int) {
        hydrationLogDao.deleteById(logID)
    }

    /**
     * Mengambil semua log hari ini untuk user tertentu.
     */
    suspend fun getTodayLogs(userID: Int): List<HydrationLogModel> {
        val today = dateFormat.format(Date())
        return hydrationLogDao.getLogsByDate(userID, today)
    }

    /**
     * Mengambil total intake hari ini untuk user tertentu.
     */
    suspend fun getTodayTotalIntake(userID: Int): Int {
        val today = dateFormat.format(Date())
        return hydrationLogDao.getTotalIntakeByDate(userID, today)
    }

    /**
     * Mengambil log dalam rentang tanggal tertentu (untuk laporan bulanan).
     */
    suspend fun getLogsBetweenDates(userID: Int, startDate: String, endDate: String): List<HydrationLogModel> {
        return hydrationLogDao.getLogsBetweenDates(userID, startDate, endDate)
    }

    /**
     * Pilihan volume cepat yang disediakan ke UI (QuickWaterButton).
     */
    fun getQuickOptions(): List<Int> {
        return listOf(150, 200, 250, 350, 500)
    }
}
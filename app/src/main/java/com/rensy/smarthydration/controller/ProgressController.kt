package com.rensy.smarthydration.controller

import com.rensy.smarthydration.database.repository.DailyProgressRepository
import com.rensy.smarthydration.database.repository.HydrationLogRepository
import com.rensy.smarthydration.model.DailyProgressModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ProgressController — GRASP Controller untuk semua operasi terkait DailyProgress.
 * Menjembatani DailyProgress dengan ProgressView/DashboardScreen.
 * Dipanggil setiap kali ada log air baru untuk recalculate progress harian.
 */
open class ProgressController(
    private val progressRepository: DailyProgressRepository,
    private val hydrationLogRepository: HydrationLogRepository
) {

    /**
     * Mengambil objek progress harian terkini dari database.
     * Jika belum ada record hari ini, buat record baru dengan totalIntake = 0.
     *
     * @param userID    ID pengguna
     * @param target    Target harian (ml) — diambil dari User.dailyTarget
     */
    suspend fun getTodayProgress(userID: Int, target: Int): DailyProgressModel {
        val existing = progressRepository.getTodayProgress(userID)
        if (existing != null) return existing

        // Buat record baru untuk hari ini
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newProgress = DailyProgressModel(
            userID = userID,
            date = today,
            totalIntake = 0,
            targetAmount = target,
            percentageValue = 0f,
            remainingAmount = target,
            isAchieved = false
        )
        progressRepository.saveOrUpdateProgress(newProgress)
        return newProgress
    }

    /**
     * Memicu sinkronisasi dan perhitungan ulang total progress setiap kali
     * ada log air baru yang masuk.
     * Mengambil total dari HYDRATION_LOG lalu update DAILY_PROGRESS.
     *
     * @param userID  ID pengguna
     * @param target  Target harian (ml)
     */
    suspend fun updateProgress(userID: Int, target: Int) {
        val newTotal = hydrationLogRepository.getTodayTotalIntake(userID)
        progressRepository.updateTodayProgress(userID, newTotal, target)
    }

    /**
     * Mengambil data progress dalam rentang tanggal untuk keperluan laporan.
     */
    suspend fun getProgressBetweenDates(
        userID: Int,
        startDate: String,
        endDate: String
    ): List<DailyProgressModel> {
        return progressRepository.getProgressBetweenDates(userID, startDate, endDate)
    }

    /**
     * Mengambil jumlah hari target terpenuhi dalam rentang tanggal.
     */
    suspend fun countAchievedDays(userID: Int, startDate: String, endDate: String): Int {
        return progressRepository.countAchievedDays(userID, startDate, endDate)
    }

    /**
     * Menghitung rata-rata intake harian dari list progress.
     */
    fun calculateAverageIntake(progressList: List<DailyProgressModel>): Float {
        if (progressList.isEmpty()) return 0f
        return progressList.sumOf { it.totalIntake }.toFloat() / progressList.size
    }

    /**
     * Helper: format tanggal pertama dan terakhir bulan tertentu.
     * Digunakan untuk query laporan bulanan.
     */
    fun getMonthRange(year: Int, month: Int): Pair<String, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val start = sdf.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = sdf.format(cal.time)
        return Pair(start, end)
    }
}
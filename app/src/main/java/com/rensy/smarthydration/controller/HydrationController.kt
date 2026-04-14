package com.rensy.smarthydration.controller

import com.rensy.smarthydration.database.repository.DailyProgressRepository
import com.rensy.smarthydration.database.repository.HydrationLogRepository
import com.rensy.smarthydration.model.HydrationLogModel

/**
 * HydrationController — GRASP Controller untuk semua operasi terkait HydrationLog.
 * Mengelola operasi CRUD pada log konsumsi air, dan memicu update DailyProgress
 * setiap kali ada log baru masuk atau dihapus.
 */
class HydrationController(
    private val hydrationLogRepository: HydrationLogRepository,
    private val dailyProgressRepository: DailyProgressRepository
) {

    /**
     * Mencatat volume air yang diminum dan menyimpannya ke log.
     * Setelah disimpan, otomatis memperbarui DailyProgress.
     *
     * @param userID      ID pengguna
     * @param amount      Volume air dalam ml (harus 0 < amount ≤ 2000)
     * @param targetAmount Target harian pengguna (ml) — untuk update progress
     * @return true jika berhasil, false jika validasi gagal
     */
    suspend fun logWater(userID: Int, amount: Int, targetAmount: Int): Boolean {
        if (amount <= 0 || amount > 2000) return false

        hydrationLogRepository.saveLog(userID, amount)

        // Update DailyProgress setelah log disimpan
        val newTotal = hydrationLogRepository.getTodayTotalIntake(userID)
        dailyProgressRepository.updateTodayProgress(userID, newTotal, targetAmount)

        return true
    }

    /**
     * Menghapus entri log air tertentu jika terjadi kesalahan input.
     * Setelah dihapus, DailyProgress di-recalculate.
     *
     * @param logID        ID log yang akan dihapus
     * @param userID       ID pengguna (untuk recalculate progress)
     * @param targetAmount Target harian pengguna (ml)
     */
    suspend fun deleteLog(logID: Int, userID: Int, targetAmount: Int) {
        hydrationLogRepository.deleteLog(logID)

        // Recalculate DailyProgress setelah log dihapus
        val newTotal = hydrationLogRepository.getTodayTotalIntake(userID)
        dailyProgressRepository.updateTodayProgress(userID, newTotal, targetAmount)
    }

    /**
     * Mengambil daftar seluruh catatan minum pada hari ini.
     *
     * @param userID ID pengguna
     * @return List<HydrationLog> diurutkan dari paling lama ke paling baru
     */
    suspend fun getTodayLogs(userID: Int): List<HydrationLogModel> {
        return hydrationLogRepository.getTodayLogs(userID)
    }

    /**
     * Menyediakan daftar pilihan volume air cepat (QuickWaterButton) untuk UI.
     * @return List volume dalam ml
     */
    fun getQuickOptions(): List<Int> {
        return hydrationLogRepository.getQuickOptions()
    }

    /**
     * Validasi volume yang diinput pengguna.
     * Aturan: 0 < volume ≤ 2000 ml.
     */
    fun validateAmount(amount: Int): Boolean {
        return amount in 1..2000
    }
}

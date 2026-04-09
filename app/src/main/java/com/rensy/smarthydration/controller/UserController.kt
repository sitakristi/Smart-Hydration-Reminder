package com.rensy.smarthydration.controller

import com.rensy.smarthydration.database.repository.UserRepository
import com.rensy.smarthydration.model.User
import java.text.SimpleDateFormat
import java.util.*

/**
 * UserController — GRASP Controller untuk semua operasi terkait User.
 * Satu-satunya pintu masuk untuk operasi profil pengguna.
 * Menerima event dari UserView/ProfileScreen, memproses ke Model, dan
 * menginstruksikan View untuk update.
 */
class UserController(private val userRepository: UserRepository) {

    /**
     * Menyimpan data profil pengguna baru ke database.
     * dailyTarget dihitung otomatis oleh UserRepository → User.calculateDailyTarget().
     *
     * @param name       Nama lengkap pengguna
     * @param birthDate  Tanggal lahir dalam epoch millis
     * @param gender     "male" atau "female"
     * @param weightKg   Berat badan dalam kg
     * @return userID yang di-generate, atau -1 jika validasi gagal
     */
    suspend fun saveUser(name: String, birthDate: Long, gender: String, weightKg: Float): Long {
        if (!validateInput(name, birthDate, gender)) return -1L
        val user = User(
            name = name.trim(),
            birthDate = birthDate,
            gender = gender,
            weightKg = weightKg
        )
        return userRepository.saveUser(user)
    }

    /**
     * Memperbarui data profil pengguna yang sudah ada.
     * dailyTarget dihitung ulang secara otomatis.
     */
    suspend fun updateUser(userID: Int, name: String, birthDate: Long, gender: String, weightKg: Float) {
        if (!validateInput(name, birthDate, gender)) return
        val user = User(
            userID = userID,
            name = name.trim(),
            birthDate = birthDate,
            gender = gender,
            weightKg = weightKg
        )
        userRepository.updateUser(user)
    }

    /**
     * Mengambil data pengguna dari database.
     * Karena single-user app, tidak perlu parameter userID.
     */
    suspend fun loadUser(): User? {
        return userRepository.getUser()
    }

    /**
     * Mengambil data pengguna berdasarkan userID.
     */
    suspend fun loadUser(userID: Int): User? {
        return userRepository.loadUser(userID)
    }

    /**
     * Menghitung target air harian berdasarkan data fisik.
     * Pria: weightKg × 33 ml | Wanita: weightKg × 28 ml
     * Min: 1500 ml, Max: 4000 ml
     */
    fun calculateDailyTarget(weightKg: Float, gender: String): Int {
        val raw = if (gender.lowercase() == "male") {
            (weightKg * 33).toInt()
        } else {
            (weightKg * 28).toInt()
        }
        return raw.coerceIn(1500, 4000)
    }

    /**
     * Mengecek apakah sudah ada profil user (untuk navigasi first-launch).
     */
    suspend fun hasUser(): Boolean {
        return userRepository.hasUser()
    }

    /**
     * Memvalidasi input form profil.
     * Aturan: nama tidak boleh kosong, birthDate valid (> 0), gender tidak kosong.
     */
    fun validateInput(name: String, birthDate: Long, gender: String): Boolean {
        if (name.isBlank()) return false
        if (birthDate <= 0L) return false
        if (gender.isBlank()) return false
        // Pastikan birthDate bukan di masa depan
        if (birthDate > System.currentTimeMillis()) return false
        return true
    }

    /**
     * Mengonversi epoch millis ke String tanggal yang readable untuk tampilan UI.
     */
    fun formatBirthDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(epochMillis))
    }
}

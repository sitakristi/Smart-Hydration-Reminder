package com.rensy.smarthydration.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "USER")
data class UserModel(
    @PrimaryKey(autoGenerate = true)
    val userID: Int = 0,
    val name: String,
    val birthDate: Long, // stored as epoch millis
    val gender: String,  // "male" or "female"
    val weightKg: Float,
    val dailyTarget: Int = 0
) {
    /**
     * Menghitung usia pengguna dari birthDate.
     */
    fun getAge(): Int {
        val today = java.util.Calendar.getInstance()
        val birth = java.util.Calendar.getInstance().apply {
            timeInMillis = birthDate
        }
        var age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    /**
     * Menghitung target konsumsi air harian dalam ml.
     * Pria: berat badan × 33 ml
     * Wanita: berat badan × 28 ml
     * Batas minimum: 1500 ml, maksimum: 4000 ml
     */
    fun calculateDailyTarget(): Int {
        val raw = if (gender.lowercase() == "male") {
            (weightKg * 33).toInt()
        } else {
            (weightKg * 28).toInt()
        }
        return raw.coerceIn(1500, 4000)
    }
}
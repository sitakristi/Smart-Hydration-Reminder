package com.rensy.smarthydration.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "DAILY_PROGRESS",
    foreignKeys = [
        ForeignKey(
            entity = UserModel::class,
            parentColumns = ["userID"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userID"]), Index(value = ["userID", "date"], unique = true)]
)
data class DailyProgressModel(
    @PrimaryKey(autoGenerate = true)
    val progressID: Int = 0,
    val userID: Int,
    val date: String,                  // "yyyy-MM-dd"
    val totalIntake: Int = 0,          // total air diminum (ml)
    val targetAmount: Int,             // target harian (ml)
    val percentageValue: Float = 0f,   // renamed: hindari clash dengan getPercentage()
    val remainingAmount: Int = 0,      // targetAmount - totalIntake
    val isAchieved: Boolean = false
) {
    /**
     * Menghitung total intake dari list HydrationLog hari ini.
     */
    fun calculateTotal(logs: List<HydrationLogModel>): Int {
        return logs.sumOf { it.amount }
    }

    /**
     * Menghitung persentase pencapaian secara dinamis.
     * Gunakan ini di UI, bukan field percentageValue.
     */
    fun computePercentage(): Float {
        if (targetAmount == 0) return 0f
        return (totalIntake.toFloat() / targetAmount.toFloat()) * 100f
    }

    /**
     * Menghitung sisa kebutuhan air secara dinamis.
     */
    fun computeRemainingAmount(): Int {
        return maxOf(0, targetAmount - totalIntake)
    }

    /**
     * Mengecek apakah target harian tercapai.
     */
    fun isTargetAchieved(): Boolean {
        return totalIntake >= targetAmount
    }

    /**
     * Mengembalikan salinan objek yang sudah diperbarui berdasarkan intake baru.
     */
    fun withUpdatedIntake(newTotalIntake: Int): DailyProgressModel {
        val newPercentage = if (targetAmount > 0)
            (newTotalIntake.toFloat() / targetAmount.toFloat()) * 100f
        else 0f
        val newRemaining = maxOf(0, targetAmount - newTotalIntake)
        return this.copy(
            totalIntake = newTotalIntake,
            percentageValue = newPercentage,
            remainingAmount = newRemaining,
            isAchieved = newTotalIntake >= targetAmount
        )
    }
}
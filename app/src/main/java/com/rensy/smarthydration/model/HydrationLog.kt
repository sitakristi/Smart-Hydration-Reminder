package com.rensy.smarthydration.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "HYDRATION_LOG",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userID"],
            childColumns = ["userID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userID"]), Index(value = ["logDate"])]
)
data class HydrationLog(
    @PrimaryKey(autoGenerate = true)
    val logID: Int = 0,
    val userID: Int,
    val amount: Int,               // volume air dalam ml
    val timestamp: Long = System.currentTimeMillis(), // epoch millis, default NOW
    val logDate: String            // "yyyy-MM-dd", untuk query efisien per hari
) {
    /**
     * Menyimpan entri konsumsi ke tabel HYDRATION_LOG.
     * (Delegasi ke Room melalui DAO — method ini sebagai penanda intent dari class diagram)
     */
    // save() → dieksekusi via HydrationLogDao.insert(this)

    /**
     * Menghapus entri konsumsi.
     * (Delegasi ke Room melalui DAO)
     */
    // delete() → dieksekusi via HydrationLogDao.delete(this)

    companion object {
        /**
         * Mengambil semua log pada tanggal tertentu.
         * (Delegasi ke HydrationLogDao.getLogsByDate())
         */
        // getByDate(date) → dieksekusi via HydrationLogDao.getLogsByDate(date)
    }
}
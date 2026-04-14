package com.rensy.smarthydration.database.repository

import com.rensy.smarthydration.database.dao.UserDao
import com.rensy.smarthydration.model.UserModel

class UserRepository(private val userDao: UserDao) {

    /**
     * Menyimpan pengguna baru ke database.
     * Sebelum disimpan, dailyTarget dihitung otomatis dari data fisik.
     * @return userID yang di-generate oleh database
     */
    suspend fun saveUser(user: UserModel): Long {
        val target = user.calculateDailyTarget()
        val userWithTarget = user.copy(dailyTarget = target)
        return userDao.insert(userWithTarget)
    }

    /**
     * Memperbarui data profil pengguna yang sudah ada.
     * dailyTarget dihitung ulang secara otomatis.
     */
    suspend fun updateUser(user: UserModel) {
        val target = user.calculateDailyTarget()
        val updatedUser = user.copy(dailyTarget = target)
        userDao.update(updatedUser)
    }

    /**
     * Mengambil data pengguna berdasarkan userID.
     */
    suspend fun loadUser(userID: Int): UserModel? {
        return userDao.getUserById(userID)
    }

    /**
     * Mengambil user pertama (aplikasi single-user).
     */
    suspend fun getUser(): UserModel? {
        return userDao.getUser()
    }

    /**
     * Mengecek apakah sudah ada user yang tersimpan (untuk first-launch check).
     */
    suspend fun hasUser(): Boolean {
        return userDao.getUserCount() > 0
    }
}
package com.rensy.smarthydration.database.dao

import androidx.room.*
import com.rensy.smarthydration.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM USER WHERE userID = :userID LIMIT 1")
    suspend fun getUserById(userID: Int): User?

    /**
     * Ambil user pertama yang ada — karena aplikasi single-user.
     */
    @Query("SELECT * FROM USER LIMIT 1")
    suspend fun getUser(): User?

    @Query("SELECT COUNT(*) FROM USER")
    suspend fun getUserCount(): Int
}
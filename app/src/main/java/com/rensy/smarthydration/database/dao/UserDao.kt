package com.rensy.smarthydration.database.dao

import androidx.room.*
import com.rensy.smarthydration.model.UserModel

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserModel): Long

    @Update
    suspend fun update(user: UserModel)

    @Delete
    suspend fun delete(user: UserModel)

    @Query("SELECT * FROM USER WHERE userID = :userID LIMIT 1")
    suspend fun getUserById(userID: Int): UserModel?

    /**
     * Ambil user pertama yang ada — karena aplikasi single-user.
     */
    @Query("SELECT * FROM USER LIMIT 1")
    suspend fun getUser(): UserModel?

    @Query("SELECT COUNT(*) FROM USER")
    suspend fun getUserCount(): Int
}
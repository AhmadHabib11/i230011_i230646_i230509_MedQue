package com.codeclinic.i230011_i230646_i230509_medque

import androidx.room.*

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUser(userId: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}
package com.codeclinic.i230011_i230646_i230509_medque

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val user_id: Int,
    val name: String,
    val nickname: String?,
    val email: String,
    val phone: String?,
    val profile_picture: String?,
    val created_at: String
)
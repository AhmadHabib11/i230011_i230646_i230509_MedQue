package com.codeclinic.i230011_i230646_i230509_medque

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val type: String,
    val title: String,
    val message: String,
    val appointmentId: Int?,
    val doctorName: String?,
    val isRead: Boolean,
    val createdAt: String,
    val syncedAt: Long = System.currentTimeMillis()
)
package com.codeclinic.i230011_i230646_i230509_medque

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val appointment_id: Int,  // Server appointment ID
    val patient_id: Int,
    val doctor_id: Int,
    val doctor_name: String,
    val specialization: String,
    val profile_picture: String?,
    val appointment_date: String,
    val appointment_time: String,
    val status: String,
    val notes: String?,
    val created_at: String,
    val synced: Boolean = false
)
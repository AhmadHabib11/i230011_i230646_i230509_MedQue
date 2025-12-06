package com.codeclinic.i230011_i230646_i230509_medque.models

data class AppointmentWithDoctor(
    val id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val status: String,
    val notes: String?,
    val created_at: String,
    val doctor_id: Int,
    val doctor_name: String,
    val specialization: String,
    val location: String?,
    val profile_picture: String?
)

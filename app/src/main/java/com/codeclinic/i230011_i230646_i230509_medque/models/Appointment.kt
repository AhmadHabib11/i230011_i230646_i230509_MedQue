package com.codeclinic.i230011_i230646_i230509_medque.models

data class Appointment(
    val id: Int,
    val patient_id: Int,
    val doctor_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val status: String,
    val notes: String?,
    val created_at: String
)

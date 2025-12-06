package com.codeclinic.i230011_i230646_i230509_medque.models

data class Doctor(
    val id: Int,
    val user_id: Int,
    val doctor_name: String,
    val specialization: String,
    val profile_picture: String?,
    val patients_count: Int,
    val experience_years: Int,
    val rating: Double,
    val reviews_count: Int,
    val about_me: String?,
    val working_time: String?,
    val location: String?,
    val created_at: String
)

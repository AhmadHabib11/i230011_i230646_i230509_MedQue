package com.codeclinic.i230011_i230646_i230509_medque.models

data class PatientProfile(
    val id: Int,
    val user_id: Int,
    val name: String?,
    val nickname: String?,
    val dob: String?,
    val gender: String?,
    val profile_picture: String?,
    val created_at: String,
    val email: String?,
    val joined_year: String
)

package com.codeclinic.i230011_i230646_i230509_medque

data class Appointment(
    val appointment_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val status: String,
    val notes: String?,
    val cancellation_reason: String?,
    val doctor_id: Int,
    val doctor_name: String,
    val specialization: String,
    val profile_picture: String?,
    val location: String?,
    val rating: Double,
    val reviews_count: Int
) {
    // Format date for display (e.g., "December 16, 2024 | 10:00 AM")
    fun getFormattedDateTime(): String {
        // You can customize this formatting
        return "$appointment_date | $appointment_time"
    }

    // Get full image URL
    fun getProfileImageUrl(baseUrl: String): String {
        return if (!profile_picture.isNullOrEmpty() && profile_picture != "null") {
            "$baseUrl/uploads/$profile_picture"
        } else {
            "" // Will use placeholder
        }
    }
}
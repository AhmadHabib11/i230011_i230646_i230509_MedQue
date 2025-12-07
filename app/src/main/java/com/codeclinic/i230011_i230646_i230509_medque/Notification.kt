package com.codeclinic.i230011_i230646_i230509_medque

data class Notification(
    val id: Int,
    val type: String, // 'appointment_booked', 'appointment_cancelled', 'appointment_rescheduled', 'report_uploaded', 'report_failed'
    val title: String,
    val message: String,
    val appointment_id: Int?,
    val doctor_name: String?,
    val is_read: Boolean,
    val created_at: String
) {
    fun getTimeAgo(): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val notificationTime = sdf.parse(created_at)
            val currentTime = java.util.Date()

            val diff = currentTime.time - (notificationTime?.time ?: 0)
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            when {
                days > 0 -> "${days}d"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "Just now"
            }
        } catch (e: Exception) {
            "Recently"
        }
    }

    fun isToday(): Boolean {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val notificationDate = sdf.parse(created_at.substring(0, 10))
            val today = sdf.parse(sdf.format(java.util.Date()))
            notificationDate == today
        } catch (e: Exception) {
            false
        }
    }

    fun getIconResource(): Int {
        return when (type) {
            "appointment_booked" -> R.drawable.calendar_tick
            "appointment_cancelled" -> R.drawable.calendar_remove
            "appointment_rescheduled" -> R.drawable.calendar_edit
            else -> R.drawable.calendar_tick
        }
    }

    fun getBackgroundDrawable(): Int {
        return when (type) {
            "appointment_booked" -> R.drawable.notification_circle_green
            "appointment_cancelled" -> R.drawable.notification_circle_red
            "appointment_rescheduled" -> R.drawable.notification_circle_gray
            else -> R.drawable.notification_circle_gray
        }
    }
}
package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AppointmentRepository(context: Context) {

    private val appointmentDao = AppDatabase.getDatabase(context).appointmentDao()
    private val TAG = "AppointmentRepository"

    // Get upcoming appointments from local database (for offline)
    fun getUpcomingAppointmentsFlow(userId: Int): Flow<List<AppointmentEntity>> {
        return appointmentDao.getUpcomingAppointments(userId)
    }

    // Get first upcoming appointment for home screen
    suspend fun getFirstUpcomingAppointment(userId: Int): AppointmentEntity? {
        return withContext(Dispatchers.IO) {
            appointmentDao.getFirstUpcomingAppointment(userId)
        }
    }

    // Save appointment locally after booking
    suspend fun saveAppointmentLocally(appointment: AppointmentEntity) {
        withContext(Dispatchers.IO) {
            appointmentDao.insertAppointment(appointment)
            Log.d(TAG, "Appointment saved locally: ${appointment.appointment_id}")
        }
    }

    // Save multiple appointments locally
    suspend fun saveAppointmentsLocally(appointments: List<AppointmentEntity>) {
        withContext(Dispatchers.IO) {
            appointmentDao.insertAppointments(appointments)
            Log.d(TAG, "Saved ${appointments.size} appointments locally")
        }
    }

    // Convert API appointment JSON to Room entity
    fun convertToEntity(appointment: JSONObject, userId: Int): AppointmentEntity {
        return AppointmentEntity(
            appointment_id = appointment.optInt("appointment_id", 0),
            patient_id = userId,
            doctor_id = appointment.optInt("doctor_id", 0),
            doctor_name = appointment.optString("doctor_name", ""),
            specialization = appointment.optString("specialization", ""),
            profile_picture = appointment.optString("profile_picture", ""),
            appointment_date = appointment.optString("appointment_date", ""),
            appointment_time = appointment.optString("appointment_time", ""),
            status = appointment.optString("status", "scheduled"),
            notes = appointment.optString("notes", ""),
            created_at = appointment.optString("created_at", ""),
            synced = true
        )
    }

    // Clear all appointments (for logout)
    suspend fun clearAppointments(userId: Int) {
        withContext(Dispatchers.IO) {
            appointmentDao.deleteAllAppointments(userId)
        }
    }
}
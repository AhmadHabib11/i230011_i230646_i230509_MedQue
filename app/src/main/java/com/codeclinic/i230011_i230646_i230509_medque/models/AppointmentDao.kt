package com.codeclinic.i230011_i230646_i230509_medque

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments WHERE patient_id = :userId AND appointment_date >= date('now') ORDER BY appointment_date ASC, appointment_time ASC")
    fun getUpcomingAppointments(userId: Int): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE patient_id = :userId AND appointment_date >= date('now') ORDER BY appointment_date ASC, appointment_time ASC LIMIT 1")
    suspend fun getFirstUpcomingAppointment(userId: Int): AppointmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    @Query("DELETE FROM appointments WHERE patient_id = :userId")
    suspend fun deleteAllAppointments(userId: Int)
}
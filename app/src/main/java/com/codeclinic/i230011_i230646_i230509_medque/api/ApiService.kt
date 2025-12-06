package com.codeclinic.i230011_i230646_i230509_medque.api

import com.codeclinic.i230011_i230646_i230509_medque.models.ApiResponse
import com.codeclinic.i230011_i230646_i230509_medque.models.AppointmentData
import com.codeclinic.i230011_i230646_i230509_medque.models.AppointmentsData
import com.codeclinic.i230011_i230646_i230509_medque.models.BookAppointmentRequest
import com.codeclinic.i230011_i230646_i230509_medque.models.DoctorDetailData
import com.codeclinic.i230011_i230646_i230509_medque.models.DoctorsData
import com.codeclinic.i230011_i230646_i230509_medque.models.PatientProfileData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    
    @GET("get_all_doctors.php")
    fun getAllDoctors(): Call<ApiResponse<DoctorsData>>
    
    @GET("get_all_doctors.php")
    fun getDoctorsBySpecialization(@Query("specialization") specialization: String): Call<ApiResponse<DoctorsData>>
    
    @GET("get_doctor_details.php")
    fun getDoctorDetails(@Query("doctor_id") doctorId: Int): Call<ApiResponse<DoctorDetailData>>
    
    @POST("book_appointment.php")
    fun bookAppointment(@Body request: BookAppointmentRequest): Call<ApiResponse<AppointmentData>>
    
    @GET("get_appointments.php")
    fun getAppointments(@Query("patient_id") patientId: Int): Call<ApiResponse<AppointmentsData>>
    
    @GET("get_patient_profile_home.php")
    fun getPatientProfile(@Query("user_id") userId: Int): Call<ApiResponse<PatientProfileData>>
}

package com.codeclinic.i230011_i230646_i230509_medque.models

data class AppointmentsData(
    val appointments: List<AppointmentWithDoctor>,
    val count: Int
)

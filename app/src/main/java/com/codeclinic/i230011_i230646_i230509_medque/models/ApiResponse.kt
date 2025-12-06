package com.codeclinic.i230011_i230646_i230509_medque.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

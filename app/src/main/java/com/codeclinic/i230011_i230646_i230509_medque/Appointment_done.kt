package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class Appointment_done : AppCompatActivity() {
    
    private lateinit var tvConfirmationMessage: TextView
    private lateinit var tvEditAppointment: TextView
    
    private var doctorName: String? = null
    private var appointmentDate: String? = null
    private var appointmentTime: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_appointment_done)
        
        // Get appointment details from intent
        doctorName = intent.getStringExtra("doctor_name")
        appointmentDate = intent.getStringExtra("appointment_date")
        appointmentTime = intent.getStringExtra("appointment_time")
        
        // Initialize views
        tvConfirmationMessage = findViewById(R.id.tv_confirmation_message)
        tvEditAppointment = findViewById(R.id.tv_edit_appointment)
        
        // Display appointment details
        displayAppointmentDetails()
        
        val done = findViewById<TextView>(R.id.btn_done)
        done.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
        
        tvEditAppointment.setOnClickListener {
            // Navigate back to edit the appointment
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun displayAppointmentDetails() {
        val doctor = doctorName ?: "the doctor"
        val formattedDate = formatDate(appointmentDate)
        val formattedTime = formatTime(appointmentTime)
        
        val message = "Your appointment with $doctor is confirmed for $formattedDate at $formattedTime."
        tvConfirmationMessage.text = message
    }
    
    private fun formatDate(dateString: String?): String {
        if (dateString == null) return "the selected date"
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
    
    private fun formatTime(timeString: String?): String {
        if (timeString == null) return "the selected time"
        
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = inputFormat.parse(timeString)
            outputFormat.format(time ?: Date())
        } catch (e: Exception) {
            timeString
        }
    }
}
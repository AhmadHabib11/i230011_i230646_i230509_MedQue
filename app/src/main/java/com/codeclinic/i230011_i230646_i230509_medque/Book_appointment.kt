package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codeclinic.i230011_i230646_i230509_medque.api.RetrofitClient
import com.codeclinic.i230011_i230646_i230509_medque.models.ApiResponse
import com.codeclinic.i230011_i230646_i230509_medque.models.AppointmentData
import com.codeclinic.i230011_i230646_i230509_medque.models.BookAppointmentRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Book_appointment : AppCompatActivity() {

    private lateinit var tvMonthYear: TextView
    private lateinit var calendarGrid: GridLayout
    private lateinit var timeSlotsGrid: GridLayout
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView
    private lateinit var btnConfirm: TextView

    private var calendar = Calendar.getInstance()
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedDateView: TextView? = null
    private var selectedTimeView: TextView? = null

    private var doctorId: Int = 0
    private var userId: Int = 0 // ✅ Changed from patientId to userId

    private val timeSlots = listOf(
        "09:00:00", "09:30:00", "10:00:00", "10:30:00", "11:00:00", "11:30:00",
        "15:00:00", "15:30:00", "16:00:00", "16:30:00", "17:00:00", "17:30:00"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_appointment)

        // Get doctor ID from intent
        doctorId = intent.getIntExtra("doctor_id", 0)

        // ✅ Get user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        userId = sharedPreferences.getInt("user_id", 0)

        if (userId == 0) {
            Toast.makeText(this, "Please login to book appointment", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupClickListeners()
        updateCalendar()
        setupTimeSlots()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        tvMonthYear = findViewById(R.id.tv_month_year)
        calendarGrid = findViewById(R.id.calendar_grid)
        timeSlotsGrid = findViewById(R.id.time_slots_grid)
        btnPrevMonth = findViewById(R.id.btn_prev_month)
        btnNextMonth = findViewById(R.id.btn_next_month)
        btnConfirm = findViewById(R.id.btn_confirm)
    }

    private fun setupClickListeners() {
        val back_btn = findViewById<ImageView>(R.id.btn_back)
        back_btn.setOnClickListener {
            finish()
        }

        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        btnConfirm.setOnClickListener {
            if (validateBooking()) {
                bookAppointment()
            }
        }
    }

    private fun updateCalendar() {
        // Update month/year display
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = monthFormat.format(calendar.time)

        // Clear existing calendar
        calendarGrid.removeAllViews()

        // Get first day of month
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Get today's date
        val today = Calendar.getInstance()

        // Add empty cells before first day
        for (i in 0 until firstDayOfWeek) {
            val emptyView = TextView(this)
            emptyView.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            calendarGrid.addView(emptyView)
        }

        // Add days
        for (day in 1..daysInMonth) {
            val dateView = TextView(this)
            dateView.text = day.toString()
            dateView.gravity = android.view.Gravity.CENTER
            dateView.setPadding(16, 16, 16, 16)

            val dateParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(4, 4, 4, 4)
            }
            dateView.layoutParams = dateParams

            // Check if this date is in the past
            val dateCal = calendar.clone() as Calendar
            dateCal.set(Calendar.DAY_OF_MONTH, day)
            dateCal.set(Calendar.HOUR_OF_DAY, 0)
            dateCal.set(Calendar.MINUTE, 0)
            dateCal.set(Calendar.SECOND, 0)
            dateCal.set(Calendar.MILLISECOND, 0)

            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            if (dateCal.before(today)) {
                // Past date - gray out
                dateView.setTextColor(Color.GRAY)
                dateView.isEnabled = false
            } else {
                // Future date - selectable
                dateView.setTextColor(Color.BLACK)
                dateView.setBackgroundResource(R.drawable.unselected)

                dateView.setOnClickListener {
                    selectDate(dateView, day)
                }
            }

            calendarGrid.addView(dateView)
        }
    }

    private fun selectDate(dateView: TextView, day: Int) {
        // Deselect previous date
        selectedDateView?.setBackgroundResource(R.drawable.unselected)
        selectedDateView?.setTextColor(Color.BLACK)

        // Select new date
        dateView.setBackgroundResource(R.drawable.selected)
        dateView.setTextColor(Color.WHITE)
        selectedDateView = dateView

        // Format selected date as YYYY-MM-DD
        val selectedCal = calendar.clone() as Calendar
        selectedCal.set(Calendar.DAY_OF_MONTH, day)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = dateFormat.format(selectedCal.time)
    }

    private fun setupTimeSlots() {
        timeSlotsGrid.removeAllViews()

        val timeLabels = listOf(
            "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM",
            "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM"
        )

        for (i in timeSlots.indices) {
            val timeView = TextView(this)
            timeView.text = timeLabels[i]
            timeView.gravity = android.view.Gravity.CENTER
            timeView.setPadding(20, 24, 20, 24)
            timeView.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
            timeView.setBackgroundResource(R.drawable.unselected)

            val timeParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
            timeView.layoutParams = timeParams

            timeView.setOnClickListener {
                selectTime(timeView, timeSlots[i])
            }

            timeSlotsGrid.addView(timeView)
        }
    }

    private fun selectTime(timeView: TextView, time: String) {
        // Deselect previous time
        selectedTimeView?.setBackgroundResource(R.drawable.unselected)
        selectedTimeView?.setTextColor(ContextCompat.getColor(this, R.color.primary_green))

        // Select new time
        timeView.setBackgroundResource(R.drawable.selected)
        timeView.setTextColor(Color.WHITE)
        selectedTimeView = timeView
        selectedTime = time
    }

    private fun validateBooking(): Boolean {
        if (doctorId == 0) {
            Toast.makeText(this, "Invalid doctor selection", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedTime == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun bookAppointment() {
        btnConfirm.isEnabled = false

        // ✅ Use user_id instead of patient_id
        val request = BookAppointmentRequest(
            patient_id = userId, // This is actually user_id
            doctor_id = doctorId,
            appointment_date = selectedDate!!,
            appointment_time = selectedTime!!,
            notes = ""
        )

        RetrofitClient.apiService.bookAppointment(request).enqueue(object : Callback<ApiResponse<AppointmentData>> {
            override fun onResponse(
                call: Call<ApiResponse<AppointmentData>>,
                response: Response<ApiResponse<AppointmentData>>
            ) {
                btnConfirm.isEnabled = true

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@Book_appointment, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Book_appointment, Appointment_done::class.java)
                    intent.putExtra("doctor_name", intent.getStringExtra("doctor_name"))
                    intent.putExtra("appointment_date", selectedDate)
                    intent.putExtra("appointment_time", selectedTime)
                    startActivity(intent)
                    finish()
                } else {
                    val message = response.body()?.message ?: "Failed to book appointment"
                    Toast.makeText(this@Book_appointment, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<AppointmentData>>, t: Throwable) {
                btnConfirm.isEnabled = true
                Toast.makeText(this@Book_appointment, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
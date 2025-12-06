package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject

class CancelledAppointments : AppCompatActivity() {

    private val BASE_URL = "http://192.168.18.37/medque_app"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue
    private lateinit var appointmentsContainer: LinearLayout
    private val appointments = mutableListOf<Appointment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cancelledappointments)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelled_appointments)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        // Find the container
        appointmentsContainer = findViewById(R.id.appointmentsContainer)

        // Load appointments
        loadCancelledAppointments()

        // Set up navigation
        setupNavigation()
    }

    private fun loadCancelledAppointments() {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$BASE_URL/get_cancelled_appointments.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("CancelledAppointments", "Requesting: $url with user_id: $userId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("CancelledAppointments", "Response: $response")
                try {
                    if (response.getBoolean("success")) {
                        val dataArray = response.getJSONArray("data")
                        appointments.clear()

                        if (dataArray.length() == 0) {
                            showEmptyState()
                        } else {
                            for (i in 0 until dataArray.length()) {
                                val appointmentJson = dataArray.getJSONObject(i)
                                val appointment = Appointment(
                                    appointment_id = appointmentJson.getInt("appointment_id"),
                                    appointment_date = appointmentJson.getString("appointment_date"),
                                    appointment_time = appointmentJson.getString("appointment_time"),
                                    status = appointmentJson.getString("status"),
                                    notes = appointmentJson.optString("notes", null),
                                    cancellation_reason = appointmentJson.optString("cancellation_reason", null),
                                    doctor_id = appointmentJson.getInt("doctor_id"),
                                    doctor_name = appointmentJson.getString("doctor_name"),
                                    specialization = appointmentJson.getString("specialization"),
                                    profile_picture = appointmentJson.optString("profile_picture", null),
                                    location = appointmentJson.optString("location", ""),
                                    rating = appointmentJson.getDouble("rating"),
                                    reviews_count = appointmentJson.getInt("reviews_count")
                                )
                                appointments.add(appointment)
                            }
                            displayAppointments()
                        }
                    } else {
                        val message = response.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        showEmptyState()
                    }
                } catch (e: Exception) {
                    Log.e("CancelledAppointments", "Error parsing: ${e.message}")
                    Toast.makeText(this, "Error loading appointments", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("CancelledAppointments", "Network error: ${error.message}")
                Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun displayAppointments() {
        appointmentsContainer.removeAllViews()

        for (appointment in appointments) {
            val cardView = createAppointmentCard(appointment)
            appointmentsContainer.addView(cardView)
        }
    }

    private fun createAppointmentCard(appointment: Appointment): CardView {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_cancelled_appointment, appointmentsContainer, false) as CardView

        // Find views in the card
        val dateTimeText = cardView.findViewById<TextView>(R.id.appointmentDateTime)
        val doctorImage = cardView.findViewById<ImageView>(R.id.doctorImage)
        val doctorName = cardView.findViewById<TextView>(R.id.doctorName)
        val specialization = cardView.findViewById<TextView>(R.id.doctorSpecialization)
        val location = cardView.findViewById<TextView>(R.id.doctorLocation)

        // Set data
        dateTimeText.text = appointment.getFormattedDateTime()
        doctorName.text = "Dr. ${appointment.doctor_name}"
        specialization.text = appointment.specialization
        location.text = appointment.location ?: "Location not specified"

        // Load doctor image
        val imageUrl = appointment.getProfileImageUrl(BASE_URL)
        if (imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.doctor5)
                .error(R.drawable.doctor5)
                .into(doctorImage)
        } else {
            doctorImage.setImageResource(R.drawable.doctor5)
        }

        return cardView
    }

    private fun showEmptyState() {
        appointmentsContainer.removeAllViews()
        val emptyText = TextView(this).apply {
            text = "No cancelled appointments"
            textSize = 16f
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
        }
        appointmentsContainer.addView(emptyText)
    }

    private fun setupNavigation() {
        val tabUpcoming = findViewById<TextView>(R.id.tabUpcoming)
        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navSearch = findViewById<LinearLayout>(R.id.navSearch)
        val navCalendar = findViewById<LinearLayout>(R.id.navCalendar)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        tabUpcoming.setOnClickListener {
            startActivity(Intent(this, UpcomingAppointments::class.java))
        }

        tabCompleted.setOnClickListener {
            startActivity(Intent(this, CompletedAppointments::class.java))
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }

        navSearch.setOnClickListener {
            startActivity(Intent(this, Searchdoctor::class.java))
        }

        navCalendar.setOnClickListener {
            // Stay on current page
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
        }
    }
}
package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {

    private val BASE_URL = "http://192.168.100.22/medque_app"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue
    private lateinit var userName: TextView
    private lateinit var joinedYear: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var appointmentCard: CardView
    private lateinit var appointmentTime: TextView
    private lateinit var doctorName: TextView
    private lateinit var clinicName: TextView
    private lateinit var clinicImage: ImageView
    private lateinit var queueBtn: TextView
    private lateinit var noAppointmentsText: TextView

    private var patientId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        // Check if user is logged in
        checkLoginStatus()

        // Initialize views
        initializeViews()

        // Load user profile
        loadUserProfile()

        // Load appointments
        loadAppointments()

        // Handle edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.act_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigationButtons()
    }

    private fun initializeViews() {
        userName = findViewById(R.id.user_name)
        joinedYear = findViewById(R.id.joined_year)
        profileImage = findViewById(R.id.profile_image)
        appointmentCard = findViewById(R.id.appointment_card)
        appointmentTime = findViewById(R.id.appointment_time)
        doctorName = findViewById(R.id.doctor_name)
        clinicName = findViewById(R.id.clinic_name)
        clinicImage = findViewById(R.id.clinic_image)
        queueBtn = findViewById(R.id.queue_btn)
        noAppointmentsText = findViewById(R.id.no_appointments_text)
    }

    private fun setupNavigationButtons() {
        val uploadReportBtn = findViewById<TextView>(R.id.upload_report_btn)
        uploadReportBtn.setOnClickListener {
            val intent = Intent(this, Upload_reports::class.java)
            startActivity(intent)
        }

        val searchdoct = findViewById<ImageView>(R.id.search_doc)
        searchdoct.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
        }

        val navcalender = findViewById<ImageView>(R.id.calenderbtn)
        navcalender.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
        }

        val bookbtn = findViewById<TextView>(R.id.btnAll)
        bookbtn.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
        }

        val notifbtn = findViewById<ImageView>(R.id.notification_icon)
        notifbtn.setOnClickListener {
            val intent = Intent(this, Notifications::class.java)
            startActivity(intent)
        }

        val prof = findViewById<ImageView>(R.id.personalbtn)
        prof.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfile() {
        // First try to load from SharedPreferences (for offline)
        val savedName = sharedPreferences.getString("name", null)
        val savedProfilePic = sharedPreferences.getString("profile_picture", null)
        val savedCreatedAt = sharedPreferences.getString("created_at", null)

        // Display saved data immediately
        if (savedName != null) {
            userName.text = savedName
        }

        if (savedCreatedAt != null) {
            val year = extractYear(savedCreatedAt)
            joinedYear.text = "Joined $year"
        }

        if (savedProfilePic != null && savedProfilePic.isNotEmpty()) {
            val imageUrl = "$BASE_URL/uploads/$savedProfilePic"
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.profile_holder)
                .error(R.drawable.profile_holder)
                .into(profileImage)
        }

        // Then fetch fresh data from API
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Log.e("Home", "User ID not found")
            return
        }

        val url = "$BASE_URL/get_patient_profile.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("Home", "Loading profile for user_id: $userId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("Home", "Profile response: $response")
                try {
                    if (response.getBoolean("success")) {
                        val data = response.getJSONObject("data")

                        val name = data.optString("name", "User")
                        val nickname = data.optString("nickname", "")
                        val profilePicture = data.optString("profile_picture", "")
                        val createdAt = data.optString("created_at", "")

                        // Display name (prefer full name, fallback to nickname)
                        val displayName = if (name.isNotEmpty()) name else nickname.ifEmpty { "User" }
                        userName.text = displayName

                        // Display joined year
                        if (createdAt.isNotEmpty()) {
                            val year = extractYear(createdAt)
                            joinedYear.text = "Joined $year"

                            // Save to SharedPreferences
                            with(sharedPreferences.edit()) {
                                putString("created_at", createdAt)
                                apply()
                            }
                        }

                        // Load profile picture
                        if (profilePicture.isNotEmpty() && profilePicture != "null") {
                            val imageUrl = "$BASE_URL/uploads/$profilePicture"
                            Log.d("Home", "Loading image: $imageUrl")
                            Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.profile_holder)
                                .error(R.drawable.profile_holder)
                                .into(profileImage)
                        }

                    } else {
                        val message = response.getString("message")
                        Log.e("Home", "Failed to load profile: $message")
                    }
                } catch (e: Exception) {
                    Log.e("Home", "Error parsing profile: ${e.message}")
                }
            },
            { error ->
                Log.e("Home", "Network error loading profile: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun extractYear(dateString: String): String {
        return try {
            // Try parsing as full datetime first (YYYY-MM-DD HH:MM:SS)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val calendar = Calendar.getInstance()
            calendar.time = date ?: Date()
            calendar.get(Calendar.YEAR).toString()
        } catch (e: Exception) {
            try {
                // Fallback to date only (YYYY-MM-DD)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                val calendar = Calendar.getInstance()
                calendar.time = date ?: Date()
                calendar.get(Calendar.YEAR).toString()
            } catch (e: Exception) {
                // If all parsing fails, just extract first 4 characters
                if (dateString.length >= 4) dateString.substring(0, 4) else "2024"
            }
        }
    }

    private fun loadAppointments() {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            showNoAppointments()
            return
        }

        val url = "$BASE_URL/get_upcoming_appointments.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("Home", "Loading appointments for user_id: $userId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("Home", "Appointments response: $response")
                try {
                    if (response.getBoolean("success")) {
                        val dataArray = response.getJSONArray("data")

                        if (dataArray.length() > 0) {
                            // Show first upcoming appointment
                            val appointment = dataArray.getJSONObject(0)
                            displayAppointment(appointment)
                        } else {
                            showNoAppointments()
                        }
                    } else {
                        showNoAppointments()
                    }
                } catch (e: Exception) {
                    Log.e("Home", "Error parsing appointments: ${e.message}")
                    showNoAppointments()
                }
            },
            { error ->
                Log.e("Home", "Network error loading appointments: ${error.message}")
                showNoAppointments()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun displayAppointment(appointment: JSONObject) {
        appointmentCard.visibility = View.VISIBLE
        noAppointmentsText.visibility = View.GONE

        val appointmentDate = appointment.getString("appointment_date")
        val appointmentTimeStr = appointment.getString("appointment_time")
        val doctorNameStr = appointment.getString("doctor_name")
        val specialization = appointment.getString("specialization")
        val profilePicture = appointment.optString("profile_picture", "")

        // Format date and time
        val dateTimeText = formatAppointmentDateTime(appointmentDate, appointmentTimeStr)
        appointmentTime.text = dateTimeText

        // Set doctor name and specialization
        doctorName.text = "Checkup with Dr. $doctorNameStr"
        clinicName.text = specialization

        // Hide queue button for now
        queueBtn.visibility = View.GONE

        // Load doctor image
        if (profilePicture.isNotEmpty() && profilePicture != "null") {
            val imageUrl = "$BASE_URL/uploads/$profilePicture"
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(clinicImage)
        } else {
            clinicImage.setImageResource(R.drawable.img)
        }

        // Set click listener to view appointment details
        appointmentCard.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
        }
    }

    private fun showNoAppointments() {
        appointmentCard.visibility = View.GONE
        noAppointmentsText.visibility = View.VISIBLE
    }

    private fun formatAppointmentDateTime(dateString: String, timeString: String): String {
        return try {
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val inputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val date = inputDateFormat.parse(dateString)
            val time = inputTimeFormat.parse(timeString)

            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            calendar.time = date ?: Date()

            val dateText = when {
                calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

                calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1 -> "Tomorrow"

                else -> {
                    val outputDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    outputDateFormat.format(date ?: Date())
                }
            }

            val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeText = outputTimeFormat.format(time ?: Date())

            "$dateText, $timeText"
        } catch (e: Exception) {
            "$dateString, $timeString"
        }
    }

    private fun checkLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to home screen
        loadUserProfile()
        loadAppointments()
    }
}
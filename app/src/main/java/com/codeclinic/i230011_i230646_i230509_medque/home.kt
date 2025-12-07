package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.codeclinic.i230011_i230646_i230509_medque.api.RetrofitClient
import com.codeclinic.i230011_i230646_i230509_medque.models.ApiResponse
import com.codeclinic.i230011_i230646_i230509_medque.models.AppointmentsData
import com.codeclinic.i230011_i230646_i230509_medque.models.AppointmentWithDoctor
import com.codeclinic.i230011_i230646_i230509_medque.models.PatientProfileData
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class home : AppCompatActivity() {

    // Configuration - Toggle between implementations
    private val USE_RETROFIT = false // Set to false to use Volley implementation

    private val BASE_URL = "http://192.168.100.22/medque_app"
    private val RETROFIT_BASE_URL = "http://192.168.100.22/medque_app/uploads/"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue
    private lateinit var appointmentRepository: AppointmentRepository

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


        NotificationPermissionHelper.checkAndRequestPermission(this)
        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        if (!USE_RETROFIT) {
            requestQueue = Volley.newRequestQueue(this)
            appointmentRepository = AppointmentRepository(this)
        }

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NotificationPermissionHelper.PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d("Home", "Notification permission granted")
            } else {
                Toast.makeText(this,
                    "You won't receive appointment notifications",
                    Toast.LENGTH_SHORT).show()
            }
        }
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

    // ========== USER PROFILE LOADING ==========

    private fun loadUserProfile() {
        if (USE_RETROFIT) {
            loadUserProfileRetrofit()
        } else {
            loadUserProfileVolley()
        }
    }

    // Retrofit Implementation
    private fun loadUserProfileRetrofit() {
        // Get user ID from SharedPreferences
        val userId = sharedPreferences.getInt("userId", 1)

        // Fetch patient profile from API
        RetrofitClient.apiService.getPatientProfile(userId).enqueue(object : Callback<ApiResponse<PatientProfileData>> {
            override fun onResponse(
                call: Call<ApiResponse<PatientProfileData>>,
                response: Response<ApiResponse<PatientProfileData>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val patient = response.body()?.data?.patient
                    if (patient != null) {
                        // Display patient name
                        userName.text = patient.name ?: patient.nickname ?: "User"

                        // Display joined year
                        joinedYear.text = "Joined ${patient.joined_year}"

                        // Load profile picture if available
                        if (!patient.profile_picture.isNullOrEmpty()) {
                            val imageUrl = "$RETROFIT_BASE_URL${patient.profile_picture}"
                            Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.profile_holder)
                                .error(R.drawable.profile_holder)
                                .into(profileImage)
                        }

                        // Save to SharedPreferences for future use
                        with(sharedPreferences.edit()) {
                            putString("userName", patient.name ?: patient.nickname)
                            putString("joinedYear", "Joined ${patient.joined_year}")
                            apply()
                        }
                    }
                } else {
                    // Fallback to SharedPreferences if API fails
                    loadUserProfileFromPrefs()
                }
            }

            override fun onFailure(call: Call<ApiResponse<PatientProfileData>>, t: Throwable) {
                // Fallback to SharedPreferences if API fails
                loadUserProfileFromPrefs()
            }
        })
    }

    // Volley Implementation with Offline Support
    private fun loadUserProfileVolley() {
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

    private fun loadUserProfileFromPrefs() {
        // Get user data from SharedPreferences
        val name = sharedPreferences.getString("userName", "User")
        val joinedYearText = sharedPreferences.getString("joinedYear", "Joined 2024")

        userName.text = name
        joinedYear.text = joinedYearText
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

    // ========== APPOINTMENTS LOADING ==========

    private fun loadAppointments() {
        if (USE_RETROFIT) {
            loadAppointmentsRetrofit()
        } else {
            loadAppointmentsVolley()
        }
    }

    // Retrofit Implementation
    private fun loadAppointmentsRetrofit() {
        // Get patient ID from SharedPreferences
        patientId = sharedPreferences.getInt("userId", 1)

        RetrofitClient.apiService.getAppointments(patientId).enqueue(object : Callback<ApiResponse<AppointmentsData>> {
            override fun onResponse(
                call: Call<ApiResponse<AppointmentsData>>,
                response: Response<ApiResponse<AppointmentsData>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val appointmentsData = response.body()?.data
                    if (appointmentsData != null && appointmentsData.appointments.isNotEmpty()) {
                        displayAppointmentRetrofit(appointmentsData.appointments[0])
                    } else {
                        showNoAppointments()
                    }
                } else {
                    showNoAppointments()
                }
            }

            override fun onFailure(call: Call<ApiResponse<AppointmentsData>>, t: Throwable) {
                showNoAppointments()
                Toast.makeText(this@home, "Error loading appointments", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Volley Implementation with Room Database
    private fun loadAppointmentsVolley() {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            showNoAppointments()
            return
        }

        // FIRST: Load from local database (OFFLINE SUPPORT)
        lifecycleScope.launch {
            try {
                val localAppointment = appointmentRepository.getFirstUpcomingAppointment(userId)
                if (localAppointment != null) {
                    displayAppointmentFromEntity(localAppointment)
                    Log.d("Home", "✅ Loaded appointment from local database (OFFLINE)")
                } else {
                    showNoAppointments()
                    Log.d("Home", "No local appointments found")
                }
            } catch (e: Exception) {
                Log.e("Home", "Error loading local appointments: ${e.message}")
                showNoAppointments()
            }
        }

        // SECOND: Fetch fresh data from API and sync to database
        val url = "$BASE_URL/get_upcoming_appointments.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("Home", "Syncing appointments from API...")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("Home", "Appointments response: $response")
                try {
                    if (response.getBoolean("success")) {
                        val dataArray = response.getJSONArray("data")

                        lifecycleScope.launch {
                            val appointments = mutableListOf<AppointmentEntity>()

                            for (i in 0 until dataArray.length()) {
                                val appointmentJson = dataArray.getJSONObject(i)
                                val entity = appointmentRepository.convertToEntity(appointmentJson, userId)
                                appointments.add(entity)
                            }

                            // Save all appointments to local database
                            if (appointments.isNotEmpty()) {
                                appointmentRepository.saveAppointmentsLocally(appointments)
                                Log.d("Home", "✅ Synced ${appointments.size} appointments to database")

                                // Update UI with latest data
                                displayAppointmentFromEntity(appointments[0])
                            } else {
                                showNoAppointments()
                            }
                        }
                    } else {
                        Log.d("Home", "API returned no appointments - keeping local data")
                    }
                } catch (e: Exception) {
                    Log.e("Home", "Error parsing appointments: ${e.message}")
                }
            },
            { error ->
                Log.e("Home", "Network error (showing offline data): ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    // ========== DISPLAY APPOINTMENTS ==========

    // Display from Retrofit response
    private fun displayAppointmentRetrofit(appointment: AppointmentWithDoctor) {
        appointmentCard.visibility = View.VISIBLE
        noAppointmentsText.visibility = View.GONE

        // Format date and time
        val dateTimeText = formatAppointmentDateTime(appointment.appointment_date, appointment.appointment_time)
        appointmentTime.text = dateTimeText

        // Set doctor name and specialization
        doctorName.text = "Checkup with ${appointment.doctor_name}"
        clinicName.text = appointment.specialization

        // Hide queue button for now
        queueBtn.visibility = View.GONE

        // Load doctor image
        if (!appointment.profile_picture.isNullOrEmpty()) {
            val imageUrl = "$RETROFIT_BASE_URL${appointment.profile_picture}"
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(clinicImage)
        } else {
            clinicImage.setImageResource(R.drawable.img)
        }

        // Set click listener
        appointmentCard.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
        }
    }

    // Display from Room database entity
    private fun displayAppointmentFromEntity(appointment: AppointmentEntity) {
        appointmentCard.visibility = View.VISIBLE
        noAppointmentsText.visibility = View.GONE

        // Format date and time
        val dateTimeText = formatAppointmentDateTime(
            appointment.appointment_date,
            appointment.appointment_time
        )
        appointmentTime.text = dateTimeText

        // Set doctor name and specialization
        doctorName.text = "Checkup with Dr. ${appointment.doctor_name}"
        clinicName.text = appointment.specialization

        // Hide queue button
        queueBtn.visibility = View.GONE

        // Load doctor image
        if (!appointment.profile_picture.isNullOrEmpty() && appointment.profile_picture != "null") {
            val imageUrl = "$BASE_URL/uploads/${appointment.profile_picture}"
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(clinicImage)
        } else {
            clinicImage.setImageResource(R.drawable.img)
        }

        // Set click listener
        appointmentCard.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
        }
    }

    private fun showNoAppointments() {
        appointmentCard.visibility = View.GONE
        noAppointmentsText.visibility = View.VISIBLE
    }

    // ========== UTILITY FUNCTIONS ==========

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


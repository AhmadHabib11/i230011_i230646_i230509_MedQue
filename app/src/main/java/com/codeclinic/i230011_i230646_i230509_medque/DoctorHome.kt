package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DoctorHome : AppCompatActivity() {

    private val BASE_URL = "http://192.168.18.37/medque_app"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue

    private lateinit var doctorNameText: TextView
    private lateinit var specializationText: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logoutBtn: Button
    private lateinit var editProfileBtn: Button

    // New fields for additional info
    private lateinit var experienceText: TextView
    private lateinit var workingHoursText: TextView
    private lateinit var locationText: TextView
    private lateinit var aboutMeText: TextView
    private lateinit var patientsCountText: TextView
    private lateinit var ratingText: TextView
    private lateinit var reviewsCountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_home)

        Log.d("DoctorHome", "DoctorHome screen loaded")

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        // Initialize all views
        doctorNameText = findViewById(R.id.doctorNameText)
        specializationText = findViewById(R.id.specializationText)
        profileImageView = findViewById(R.id.doctorProfileImage)
        logoutBtn = findViewById(R.id.logoutBtn)
        editProfileBtn = findViewById(R.id.editProfileBtn)

        // Initialize new views
        experienceText = findViewById(R.id.experienceText)
        workingHoursText = findViewById(R.id.workingHoursText)
        locationText = findViewById(R.id.locationText)
        aboutMeText = findViewById(R.id.aboutMeText)
        patientsCountText = findViewById(R.id.patientsCountText)
        ratingText = findViewById(R.id.ratingText)
        reviewsCountText = findViewById(R.id.reviewsCountText)

        // Load doctor data
        loadDoctorData()

        // Edit Profile Button
        editProfileBtn.setOnClickListener {
            val userId = sharedPreferences.getInt("user_id", -1)
            if (userId != -1) {
                val intent = Intent(this, SetUpDoctorProfile::class.java)
                intent.putExtra("user_id", userId)
                intent.putExtra("is_editing", true)
                startActivity(intent)
                // Don't finish() - user will come back here after editing
            } else {
                Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout button
        logoutBtn.setOnClickListener {
            val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)
            with(sharedPreferences.edit()) {
                clear()
                putBoolean("hasSeenOnboarding", hasSeenOnboarding)
                apply()
            }
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadDoctorData() {
        val userId = sharedPreferences.getInt("user_id", -1)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        Log.d("DoctorHome", "Loading data for user_id: $userId, isLoggedIn: $isLoggedIn")

        if (userId == -1 || !isLoggedIn) {
            doctorNameText.text = "Session Expired"
            specializationText.text = "Please login again"
            Toast.makeText(this, "Session expired. Please login again", Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, Signin::class.java)
                startActivity(intent)
                finish()
            }, 2000)
            return
        }

        // Show loading state
        doctorNameText.text = "Loading..."
        specializationText.text = "Please wait..."
        experienceText.text = "Experience: Loading..."
        workingHoursText.text = "Working Hours: Loading..."
        locationText.text = "Location: Loading..."
        aboutMeText.text = "Loading..."

        val url = "$BASE_URL/get_doctor_profile.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("DoctorHome", "Requesting from: $url")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("DoctorHome", "Response: ${response.toString()}")
                try {
                    if (response.getBoolean("success")) {
                        val doctorData = response.getJSONObject("data")

                        // Save all data to SharedPreferences for offline access
                        saveDoctorDataToPrefs(doctorData)

                        // Display all data
                        displayDoctorData(doctorData)
                    } else {
                        val message = response.getString("message")
                        Log.e("DoctorHome", "Error from server: $message")

                        if (message.contains("not found", ignoreCase = true)) {
                            // Profile not found, redirect to setup
                            doctorNameText.text = "Profile Not Complete"
                            specializationText.text = "Please setup your profile"
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this, SetUpDoctorProfile::class.java)
                                intent.putExtra("user_id", userId)
                                startActivity(intent)
                                finish()
                            }, 3000)
                        } else {
                            // Try offline data for other errors too
                            tryLoadOfflineData()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DoctorHome", "Error parsing response: ${e.message}")
                    tryLoadOfflineData()
                }
            },
            { error ->
                Log.e("DoctorHome", "Network error: ${error.message}")
                // Load offline data
                tryLoadOfflineData()
            }
        )

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun saveDoctorDataToPrefs(doctorData: JSONObject) {
        with(sharedPreferences.edit()) {
            putString("doctor_name", doctorData.getString("doctor_name"))
            putString("specialization", doctorData.getString("specialization"))
            putString("profile_picture", doctorData.optString("profile_picture", ""))
            putInt("experience_years", doctorData.optInt("experience_years", 0))
            putString("working_time", doctorData.optString("working_time", ""))
            putString("location", doctorData.optString("location", ""))
            putString("about_me", doctorData.optString("about_me", ""))
            putInt("patients_count", doctorData.optInt("patients_count", 0))
            putFloat("rating", doctorData.optDouble("rating", 5.0).toFloat())
            putInt("reviews_count", doctorData.optInt("reviews_count", 0))
            apply()
        }
    }

    private fun tryLoadOfflineData() {
        val savedName = sharedPreferences.getString("doctor_name", null)
        val savedSpecialization = sharedPreferences.getString("specialization", null)
        val savedProfilePicture = sharedPreferences.getString("profile_picture", null)
        val savedExperience = sharedPreferences.getInt("experience_years", 0)
        val savedWorkingTime = sharedPreferences.getString("working_time", "")
        val savedLocation = sharedPreferences.getString("location", "")
        val savedAboutMe = sharedPreferences.getString("about_me", "")
        val savedPatientsCount = sharedPreferences.getInt("patients_count", 0)
        val savedRating = sharedPreferences.getFloat("rating", 5.0f)
        val savedReviewsCount = sharedPreferences.getInt("reviews_count", 0)

        // FIXED: Handle nullable strings properly
        doctorNameText.text = if (savedName != null) "Dr. $savedName" else "No Name"
        specializationText.text = savedSpecialization ?: "No Specialization"
        experienceText.text = "Experience: $savedExperience years"

        // FIXED: Use safe calls for nullable strings
        val workingTimeDisplay = savedWorkingTime ?: "Not specified"
        workingHoursText.text = "Working Hours: $workingTimeDisplay"

        val locationDisplay = savedLocation ?: "Not specified"
        locationText.text = "Location: $locationDisplay"

        val aboutMeDisplay = savedAboutMe ?: "No about me added yet."
        aboutMeText.text = if (aboutMeDisplay.isEmpty()) "No about me added yet." else aboutMeDisplay

        patientsCountText.text = savedPatientsCount.toString()
        ratingText.text = String.format("%.1f", savedRating)
        reviewsCountText.text = savedReviewsCount.toString()

        // Load saved profile picture
        if (savedProfilePicture != null && savedProfilePicture.isNotEmpty() && savedProfilePicture != "null") {
            val imageUrl = "$BASE_URL/uploads/$savedProfilePicture"
            Log.d("DoctorHome", "Loading offline image: $imageUrl")

            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.dp_circle)
                .error(R.drawable.dp_circle)
                .into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.dp_circle)
        }

        // Only show offline toast if this is a refresh (not initial load)
        Toast.makeText(this, "Showing offline data", Toast.LENGTH_SHORT).show()
    }

    private fun displayDoctorData(doctorData: JSONObject) {
        try {
            val doctorName = doctorData.getString("doctor_name")
            val specialization = doctorData.getString("specialization")
            val profilePicture = doctorData.optString("profile_picture", "")
            val experience = doctorData.optInt("experience_years", 0)
            val workingTime = doctorData.optString("working_time", "")
            val location = doctorData.optString("location", "")
            val aboutMe = doctorData.optString("about_me", "")
            val patientsCount = doctorData.optInt("patients_count", 0)
            val rating = doctorData.optDouble("rating", 5.0)
            val reviewsCount = doctorData.optInt("reviews_count", 0)

            Log.d("DoctorHome", "Displaying doctor data:")
            Log.d("DoctorHome", "- Name: $doctorName")
            Log.d("DoctorHome", "- Specialization: $specialization")
            Log.d("DoctorHome", "- Experience: $experience years")
            Log.d("DoctorHome", "- Working Time: '$workingTime'")
            Log.d("DoctorHome", "- Location: '$location'")
            Log.d("DoctorHome", "- About Me: '${aboutMe.take(50)}...'")

            // Update all text views
            runOnUiThread {
                doctorNameText.text = "Dr. $doctorName"
                specializationText.text = specialization

                // ✅ FIX: Update ALL the new fields
                experienceText.text = "Experience: $experience years"

                val workingTimeDisplay = if (workingTime.isNotEmpty()) workingTime else "Not specified"
                workingHoursText.text = "Working Hours: $workingTimeDisplay"

                val locationDisplay = if (location.isNotEmpty()) location else "Not specified"
                locationText.text = "Location: $locationDisplay"

                val aboutMeDisplay = if (aboutMe.isNotEmpty()) aboutMe else "No about me added yet."
                aboutMeText.text = aboutMeDisplay

                patientsCountText.text = patientsCount.toString()
                ratingText.text = String.format("%.1f", rating)
                reviewsCountText.text = reviewsCount.toString()
            }

            // Load profile picture
            if (profilePicture.isNotEmpty() && profilePicture != "null") {
                val imageUrl = "$BASE_URL/uploads/$profilePicture"
                Log.d("DoctorHome", "Loading image from: $imageUrl")

                runOnUiThread {
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.dp_circle)
                        .error(R.drawable.dp_circle)
                        .into(profileImageView, object : com.squareup.picasso.Callback {
                            override fun onSuccess() {
                                Log.d("DoctorHome", "✅ Image loaded successfully")
                            }
                            override fun onError(e: Exception?) {
                                Log.e("DoctorHome", "❌ Picasso error: ${e?.message}")
                                profileImageView.setImageResource(R.drawable.dp_circle)
                            }
                        })
                }
            } else {
                Log.d("DoctorHome", "No profile picture, setting default")
                runOnUiThread {
                    profileImageView.setImageResource(R.drawable.dp_circle)
                }
            }
        } catch (e: Exception) {
            Log.e("DoctorHome", "Error displaying data: ${e.message}")
            runOnUiThread {
                doctorNameText.text = "Error Displaying Profile"
                specializationText.text = "Please refresh"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("DoctorHome", "onResume called - reloading data")
        loadDoctorData()
    }
}
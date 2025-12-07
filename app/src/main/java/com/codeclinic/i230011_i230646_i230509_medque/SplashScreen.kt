package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "SplashScreen"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splashscreen)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashscreen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSessionAndNavigate()
        }, 3000)
    }

    private fun checkUserSessionAndNavigate() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val userId = sharedPreferences.getInt("user_id", -1)
        val userType = sharedPreferences.getString("user_type", "")
        val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

        // Get doctor-specific data to check if profile is really completed
        val doctorName = sharedPreferences.getString("doctor_name", "")
        val doctorHasProfile = sharedPreferences.getBoolean("profile_completed", false)

        // Debug logs
        Log.d(TAG, "=== SESSION CHECK ===")
        Log.d(TAG, "isLoggedIn: $isLoggedIn")
        Log.d(TAG, "userId: $userId")
        Log.d(TAG, "userType: $userType")
        Log.d(TAG, "hasSeenOnboarding: $hasSeenOnboarding")
        Log.d(TAG, "doctorName: $doctorName")
        Log.d(TAG, "doctorHasProfile: $doctorHasProfile")
        Log.d(TAG, "====================")

        if (!hasSeenOnboarding) {
            // First time user - go to onboarding
            Log.d(TAG, "First time user, going to Onboarding")
            val intent = Intent(this, Onboarding::class.java)
            startActivity(intent)
            finish()
        } else if (isLoggedIn && userId != -1 && !userType.isNullOrEmpty()) {
            // User is logged in - navigate based on user type
            when (userType) {
                "doctor" -> {
                    Log.d(TAG, "User is a DOCTOR")

                    // FIX: Check if doctor actually has a profile (name is a good indicator)
                    val hasDoctorProfile = doctorName?.isNotEmpty() == true

                    if (hasDoctorProfile || doctorHasProfile) {
                        // Doctor has completed profile - go to DoctorHome
                        Log.d(TAG, "Doctor has profile, going to DoctorHome")
                        val intent = Intent(this, DoctorHome::class.java)
                        startActivity(intent)
                    } else {
                        // Doctor without profile - go to profile setup
                        Log.d(TAG, "Doctor needs profile setup, going to SetUpDoctorProfile")
                        val intent = Intent(this, SetUpDoctorProfile::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("is_editing", false)
                        startActivity(intent)
                    }
                }
                "patient" -> {
                    Log.d(TAG, "User is a PATIENT")
                    val patientName = sharedPreferences.getString("name", "")
                    val patientHasProfile = sharedPreferences.getBoolean("profile_completed", false)

                    if ((patientName?.isNotEmpty() == true) || patientHasProfile) {
                        // Patient with completed profile - go to Home
                        Log.d(TAG, "Patient has completed profile, going to Home")
                        val intent = Intent(this, home::class.java)
                        startActivity(intent)
                    } else {
                        // Patient without profile - go to profile setup
                        Log.d(TAG, "Patient needs profile setup, going to SetUpProfile")
                        val intent = Intent(this, SetUpProfile::class.java)
                        intent.putExtra("user_id", userId)
                        startActivity(intent)
                    }
                }
                else -> {
                    // Unknown user type - go to login
                    Log.d(TAG, "Unknown user type, going to Signin")
                    val intent = Intent(this, Signin::class.java)
                    startActivity(intent)
                }
            }
            finish()
        } else {
            // Not logged in or invalid session - go to login
            Log.d(TAG, "User not logged in or invalid session, going to Signin")
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }
}
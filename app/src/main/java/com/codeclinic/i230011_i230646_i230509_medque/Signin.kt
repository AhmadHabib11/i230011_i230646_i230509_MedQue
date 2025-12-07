package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONObject

class Signin : AppCompatActivity() {

    private val BASE_URL = "http://192.168.1.4/medque_app"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue
    private var userType = "patient"

    companion object {
        private const val TAG = "SigninActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signin)

        requestQueue = Volley.newRequestQueue(this)
        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val signinbtn = findViewById<Button>(R.id.signInButton)
        val signupbtn = findViewById<TextView>(R.id.signUpLink)
        val forgotpasswordbtn = findViewById<TextView>(R.id.forgotPasswordLink)
        val userTypeSwitch = findViewById<SwitchMaterial>(R.id.userTypeSwitch)
        val userTypeLabel = findViewById<TextView>(R.id.userTypeLabel)

        userTypeSwitch.setOnCheckedChangeListener { _, isChecked ->
            userType = if (isChecked) {
                userTypeLabel.text = "Sign in as Doctor"
                "doctor"
            } else {
                userTypeLabel.text = "Sign in as Patient"
                "patient"
            }
        }

        signinbtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signinbtn.isEnabled = false
            signinbtn.text = "Signing In..."

            loginUser(email, password, userType) { success, message, userData ->
                runOnUiThread {
                    signinbtn.isEnabled = true
                    signinbtn.text = "Sign In"

                    if (success && userData != null) {
                        saveUserData(userData)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        navigateToHome(userData)
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        signupbtn.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
            finish()
        }

        forgotpasswordbtn.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginUser(email: String, password: String, userType: String, callback: (Boolean, String, JSONObject?) -> Unit) {
        val url = "$BASE_URL/login.php"

        Log.d(TAG, "Login URL: $url")
        Log.d(TAG, "Email: $email, Type: $userType")

        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("user_type", userType)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, json,
            { response ->
                Log.d(TAG, "Response: $response")
                try {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")
                    val data = if (success && response.has("data")) {
                        response.getJSONObject("data")
                    } else null
                    callback(success, message, data)
                } catch (e: Exception) {
                    Log.e(TAG, "Parse error", e)
                    callback(false, "Error: ${e.message}", null)
                }
            },
            { error ->
                Log.e(TAG, "Network error", error)

                val msg = when {
                    error.networkResponse != null -> {
                        val body = String(error.networkResponse.data)
                        Log.e(TAG, "Error body: $body")
                        "Server error: $body"
                    }
                    else -> "Connection failed: ${error.message}"
                }

                callback(false, msg, null)
            }
        ) {
            override fun getHeaders() = hashMapOf(
                "Content-Type" to "application/json"
            )
        }

        request.retryPolicy = DefaultRetryPolicy(30000, 0, 1f)
        request.setShouldCache(false)
        requestQueue.add(request)
    }

    private fun saveUserData(data: JSONObject) {
        Log.d(TAG, "Saving user data: $data")

        with(sharedPreferences.edit()) {
            // Basic user info from users table
            putInt("user_id", data.optInt("id"))
            putString("email", data.optString("email"))
            putString("user_type", data.optString("user_type"))
            putBoolean("isLoggedIn", true)
            putBoolean("profile_completed", data.optBoolean("has_profile"))
            putBoolean("hasSeenOnboarding", true)

            // Patient-specific data from patients table
            if (data.optString("user_type") == "patient") {
                putString("name", data.optString("name", ""))
                putString("nickname", data.optString("nickname", ""))
                putString("dob", data.optString("dob", ""))
                putString("gender", data.optString("gender", ""))
                putString("profile_picture", data.optString("profile_picture", ""))
            }

            // Doctor-specific data from doctors table
            if (data.optString("user_type") == "doctor") {
                putString("doctor_name", data.optString("doctor_name", ""))
                putString("specialization", data.optString("specialization", ""))
                putInt("experience_years", data.optInt("experience_years", 0))
                putString("working_time", data.optString("working_time", ""))
                putString("location", data.optString("location", ""))
                putString("about_me", data.optString("about_me", ""))
                putInt("patients_count", data.optInt("patients_count", 0))
                putString("rating", data.optString("rating", "0.0"))
                putInt("reviews_count", data.optInt("reviews_count", 0))
                putString("profile_picture", data.optString("profile_picture", ""))
            }

            apply()
        }

        Log.d(TAG, "User data saved successfully")
    }

    private fun navigateToHome(data: JSONObject) {
        val intent = when (data.optString("user_type")) {
            "doctor" -> Intent(this, DoctorHome::class.java)
            else -> Intent(this, Home::class.java)
        }
        startActivity(intent)
        finish()
    }
}
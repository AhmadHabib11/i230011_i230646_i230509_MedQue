package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONObject

class Signup : AppCompatActivity() {

    private val BASE_URL = "http://192.168.100.22/medque_app"
    private lateinit var requestQueue: com.android.volley.RequestQueue
    private lateinit var sharedPreferences: SharedPreferences
    private var userType = "patient"

    companion object {
        private const val TAG = "SignupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        requestQueue = Volley.newRequestQueue(this)
        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val createaccbtn = findViewById<Button>(R.id.createAccountButton)
        val signinlink = findViewById<TextView>(R.id.signInLink)
        val userTypeSwitch = findViewById<SwitchMaterial>(R.id.userTypeSwitch)
        val userTypeLabel = findViewById<TextView>(R.id.userTypeLabel)

        userTypeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                userType = "doctor"
                userTypeLabel.text = "Sign up as Doctor"
            } else {
                userType = "patient"
                userTypeLabel.text = "Sign up as Patient"
            }
        }

        createaccbtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createaccbtn.isEnabled = false
            createaccbtn.text = "Creating..."

            signupUserWithVolley(email, password, userType) { success, message, userId ->
                createaccbtn.isEnabled = true
                createaccbtn.text = "Create Account"

                if (success && userId != null) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    // ✅ Save initial user data to SharedPreferences
                    with(sharedPreferences.edit()) {
                        putInt("user_id", userId)
                        putString("email", email)
                        putString("user_type", userType)
                        putBoolean("hasSeenOnboarding", true)
                        putBoolean("isLoggedIn", false) // Not fully logged in until profile is complete
                        apply()
                    }

                    Log.d(TAG, "User data saved after signup. UserID: $userId")

                    // Navigate to profile setup
                    if (userType == "doctor") {
                        val intent = Intent(this, SetUpDoctorProfile::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, SetUpProfile::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    }
                    finish()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        signinlink.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signupUserWithVolley(email: String, password: String, userType: String, callback: (Boolean, String, Int?) -> Unit) {
        val url = "$BASE_URL/signup.php"

        Log.d(TAG, "Starting signup request to: $url")
        Log.d(TAG, "Email: $email, UserType: $userType")

        val jsonObject = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("user_type", userType)
        }

        Log.d(TAG, "Request JSON: $jsonObject")

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d(TAG, "Success response: $response")
                try {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")
                    val userId = if (success && response.has("data")) {
                        response.getJSONObject("data").getInt("user_id")
                    } else null
                    callback(success, message, userId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing response", e)
                    callback(false, "Error parsing server response: ${e.message}", null)
                }
            },
            { error ->
                Log.e(TAG, "Volley error occurred", error)
                Log.e(TAG, "Error details - Code: ${error.networkResponse?.statusCode}")

                val errorMessage = when {
                    error.networkResponse != null -> {
                        val statusCode = error.networkResponse.statusCode
                        val errorBody = String(error.networkResponse.data, Charsets.UTF_8)
                        Log.e(TAG, "Error response body: $errorBody")
                        "Server error ($statusCode): $errorBody"
                    }
                    error.cause != null -> {
                        Log.e(TAG, "Error cause: ${error.cause}")
                        "Connection error: ${error.cause?.message}"
                    }
                    else -> {
                        Log.e(TAG, "Unknown error: ${error.message}")
                        "Network error: ${error.message ?: "Unable to connect to server"}"
                    }
                }

                callback(false, errorMessage, null)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                Log.d(TAG, "Request headers: $headers")
                return headers
            }
        }

        // Set timeout to 30 seconds
        jsonObjectRequest.setShouldCache(false)
        requestQueue.add(jsonObjectRequest)
        Log.d(TAG, "Request added to queue")
    }
}
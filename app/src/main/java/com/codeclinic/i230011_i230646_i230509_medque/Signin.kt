package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Signin : AppCompatActivity() {

    private val BASE_URL = "http://192.168.1.3/medque_app"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signin)

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this)
        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val signinbtn = findViewById<Button>(R.id.signInButton)
        val signupbtn = findViewById<TextView>(R.id.signUpLink)
        val forgotpasswordbtn = findViewById<TextView>(R.id.forgotPasswordLink)

        signinbtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validate inputs
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button during request
            signinbtn.isEnabled = false
            signinbtn.text = "Signing In..."

            // Make login request with Volley
            loginUserWithVolley(email, password) { success, message, userData ->
                signinbtn.isEnabled = true
                signinbtn.text = "Sign In"

                if (success && userData != null) {
                    // Save user data to SharedPreferences
                    saveUserData(userData)

                    // Mark that user has seen onboarding (they came through signin)
                    with(sharedPreferences.edit()) {
                        putBoolean("hasSeenOnboarding", true)
                        apply()
                    }

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    // Navigate to Home
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }

        signupbtn.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }

        forgotpasswordbtn.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loginUserWithVolley(email: String, password: String, callback: (Boolean, String, JSONObject?) -> Unit) {
        val url = "$BASE_URL/login.php"
        val jsonObject = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                val success = response.getBoolean("success")
                val message = response.getString("message")
                val userData = if (success && response.has("data")) {
                    response.getJSONObject("data")
                } else null
                callback(success, message, userData)
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                callback(false, errorMessage, null)
            }
        )

        // Add to request queue
        requestQueue.add(jsonObjectRequest)
    }

    private fun saveUserData(userData: JSONObject) {
        with(sharedPreferences.edit()) {
            putInt("user_id", userData.getInt("id"))
            putString("email", userData.getString("email"))

            if (userData.has("name")) {
                putString("name", userData.getString("name"))
            }

            if (userData.has("nickname")) {
                putString("nickname", userData.getString("nickname"))
            }

            if (userData.has("dob")) {
                putString("dob", userData.getString("dob"))
            }

            if (userData.has("gender")) {
                putString("gender", userData.getString("gender"))
            }

            if (userData.has("profile_picture")) {
                putString("profile_picture", userData.getString("profile_picture"))
            }

            putBoolean("isLoggedIn", true)
            apply()
        }
    }
}
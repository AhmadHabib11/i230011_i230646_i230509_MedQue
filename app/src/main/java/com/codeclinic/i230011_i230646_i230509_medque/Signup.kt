package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Signup : AppCompatActivity() {

    private val BASE_URL = "http://192.168.18.37/medque_app"
    private lateinit var requestQueue: com.android.volley.RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val createaccbtn = findViewById<Button>(R.id.createAccountButton)
        val signinlink = findViewById<TextView>(R.id.signInLink)

        createaccbtn.setOnClickListener {
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

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button during request
            createaccbtn.isEnabled = false
            createaccbtn.text = "Creating..."

            // Make signup request with Volley
            signupUserWithVolley(email, password) { success, message, userId ->
                createaccbtn.isEnabled = true
                createaccbtn.text = "Create Account"

                if (success && userId != null) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    // Navigate to profile setup with user ID
                    val intent = Intent(this, SetUpProfile::class.java)
                    intent.putExtra("user_id", userId)
                    intent.putExtra("email", email)
                    startActivity(intent)
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

    private fun signupUserWithVolley(email: String, password: String, callback: (Boolean, String, Int?) -> Unit) {
        val url = "$BASE_URL/signup.php"
        val jsonObject = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                val success = response.getBoolean("success")
                val message = response.getString("message")
                val userId = if (success && response.has("data")) {
                    response.getJSONObject("data").getInt("user_id")
                } else null
                callback(success, message, userId)
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                callback(false, errorMessage, null)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
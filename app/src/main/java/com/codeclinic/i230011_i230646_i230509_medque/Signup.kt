package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class Signup : AppCompatActivity() {


    private val BASE_URL = "http://192.168.18.37/medque_app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

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

            // Make signup request
            signupUser(email, password) { success, message, userId ->
                runOnUiThread {
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
        }

        signinlink.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signupUser(email: String, password: String, callback: (Boolean, String, Int?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$BASE_URL/signup.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                // Create JSON payload
                val jsonPayload = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                // Send request
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                // Read response
                val responseCode = connection.responseCode
                val reader = if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream))
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream))
                }

                val response = reader.use { it.readText() }
                val jsonResponse = JSONObject(response)

                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")
                val userId = if (success && jsonResponse.has("data")) {
                    jsonResponse.getJSONObject("data").getInt("user_id")
                } else null

                withContext(Dispatchers.Main) {
                    callback(success, message, userId)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(false, "Network error: ${e.message}", null)
                }
            }
        }
    }
}
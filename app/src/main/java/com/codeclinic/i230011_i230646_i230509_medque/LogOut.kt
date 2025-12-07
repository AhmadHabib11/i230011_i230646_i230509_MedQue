package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso

class LogOut : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userName: TextView
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.logout)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        // Initialize views from the profile section (inside the logout layout)
        userName = findViewById(R.id.userName)
        profileImage = findViewById(R.id.profileImage)
        val phoneNumber = findViewById<TextView>(R.id.phoneNumber)

        // Remove phone number as requested
        phoneNumber.visibility = TextView.GONE

        // Load user data
        loadUserData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.logout_screen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logoutBtn = findViewById<TextView>(R.id.logoutConfirmButton)
        logoutBtn.setOnClickListener {
            // Save hasSeenOnboarding before clearing
            val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

            // Clear ALL user data EXCEPT hasSeenOnboarding
            with(sharedPreferences.edit()) {
                clear() // Clear everything first
                // Restore hasSeenOnboarding
                putBoolean("hasSeenOnboarding", hasSeenOnboarding)
                apply()
            }

            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }

        // Cancel button click listener
        val cancelButton = findViewById<TextView>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            finish() // Go back to the previous screen (Profile)
        }
    }

    private fun loadUserData() {
        val name = sharedPreferences.getString("name", "")
        val nickname = sharedPreferences.getString("nickname", "")
        val profilePicture = sharedPreferences.getString("profile_picture", "")

        // Display name (use nickname if available, otherwise use name)
        val displayName = if (!nickname.isNullOrEmpty()) nickname else name
        userName.text = displayName ?: "User"

        // Load profile image if available using Picasso
        if (!profilePicture.isNullOrEmpty()) {
            Picasso.get()
                .load("$BASE_URL/uploads/$profilePicture")
                .placeholder(R.drawable.dp)
                .error(R.drawable.dp)
                .into(profileImage)
        }
    }

    companion object {
        private const val BASE_URL = "http://192.168.100.22/medque_app"
    }
}
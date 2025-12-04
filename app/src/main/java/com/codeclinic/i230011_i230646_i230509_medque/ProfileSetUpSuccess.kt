package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileSetUpSuccess : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profilesetupsuccess)

        // Clear any previous session
        val sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", false) // Ensure user needs to login
            apply()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // Go to Signin screen
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }, 3000) // Reduced to 3 seconds

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profilesetupsuccess)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
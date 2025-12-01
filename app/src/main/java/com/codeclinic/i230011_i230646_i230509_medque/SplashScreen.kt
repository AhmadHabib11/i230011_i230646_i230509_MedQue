package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

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
            checkLoginStatus()
        }, 3000)
    }

    private fun checkLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val hasSeenOnboarding = sharedPreferences.getBoolean("hasSeenOnboarding", false)

        // Debug: Print the values
        println("DEBUG: isLoggedIn = $isLoggedIn")
        println("DEBUG: hasSeenOnboarding = $hasSeenOnboarding")

        if (isLoggedIn) {
            // User is already logged in, go directly to Home
            println("DEBUG: Going to Home (user is logged in)")
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        } else {
            // User is not logged in
            if (hasSeenOnboarding) {
                // User has seen onboarding before, go to Signin
                println("DEBUG: Going to Signin (has seen onboarding)")
                val intent = Intent(this, Signin::class.java)
                startActivity(intent)
            } else {
                // First time user, show onboarding
                println("DEBUG: Going to Onboarding (first time)")
                val intent = Intent(this, Onboarding::class.java)
                startActivity(intent)
            }
        }
        finish()
    }
}
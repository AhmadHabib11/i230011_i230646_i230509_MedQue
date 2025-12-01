package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Onboarding : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.onboarding)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboarding)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn = findViewById<Button>(R.id.getStartedButton)
        btn.setOnClickListener {
            // Mark that user has seen onboarding
            with(sharedPreferences.edit()) {
                putBoolean("hasSeenOnboarding", true)
                apply()
            }

            println("DEBUG: Onboarding - Going to Signup")

            // Navigate to Signup
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }
    }
}
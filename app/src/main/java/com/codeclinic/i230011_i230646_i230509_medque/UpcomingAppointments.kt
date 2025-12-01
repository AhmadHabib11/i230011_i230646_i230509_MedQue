package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UpcomingAppointments : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.upcomingappointments)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.upcomingappointments)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up click listeners for navigation
        val tabCompleted = findViewById<TextView>(R.id.tabCompleted)
        val tabCancelled = findViewById<TextView>(R.id.tabCancelled)

        tabCompleted.setOnClickListener {
            val intent = Intent(this, CompletedAppointments::class.java)
            startActivity(intent)
        }

        tabCancelled.setOnClickListener {
            val intent = Intent(this, CancelledAppointments::class.java)
            startActivity(intent)
        }

        // Calendar navigation click listener
        val navCalendar = findViewById<LinearLayout>(R.id.navCalendar)
        navCalendar.setOnClickListener {
            val intent = Intent(this, CompletedAppointments::class.java)
            startActivity(intent)
        }

        val navHome = findViewById<LinearLayout>(R.id.navHome)
        navHome.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        // Search icon click listener
        val navSearch = findViewById<LinearLayout>(R.id.navSearch)
        navSearch.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
        }

        // Profile navigation click listener
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        navProfile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}
package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Profile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_screen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Search icon click listener
        val navSearch = findViewById<LinearLayout>(R.id.navSearch)
        navSearch.setOnClickListener {
            val intent = Intent(this, SearchDoctors::class.java)
            startActivity(intent)
        }

        // Calendar navigation click listener
        val navCalendar = findViewById<LinearLayout>(R.id.navCalendar)
        navCalendar.setOnClickListener {
            val intent = Intent(this, CompletedAppointments::class.java)
            startActivity(intent)
        }

        // Edit Profile option click listener
        val editProfileOption = findViewById<RelativeLayout>(R.id.editProfileOption)
        editProfileOption.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        // Logout option click listener
        val logoutOption = findViewById<RelativeLayout>(R.id.logoutOption)
        logoutOption.setOnClickListener {
            val intent = Intent(this, LogOut::class.java)
            startActivity(intent)
        }
    }
}
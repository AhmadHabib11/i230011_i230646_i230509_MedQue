package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso

class Profile : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userName: TextView
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.profile)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        // Initialize views
        userName = findViewById(R.id.userName)
        profileImage = findViewById(R.id.profileImage)
        val phoneNumber = findViewById<TextView>(R.id.phoneNumber)

        // Remove phone number as requested
        phoneNumber.visibility = TextView.GONE

        // Load user data
        loadUserData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_screen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHome = findViewById<LinearLayout>(R.id.navHome)
        navHome.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
        val searchDoctor = findViewById<LinearLayout>(R.id.navSearch)
        searchDoctor.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
        }
        val upcapp = findViewById<LinearLayout>(R.id.navCalendar)
        upcapp.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
        }

        // Calendar navigation click listener
        val navCalendar = findViewById<LinearLayout>(R.id.navCalendar)
        navCalendar.setOnClickListener {
            val intent = Intent(this, CompletedAppointments::class.java)
            startActivity(intent)
        }

        // Edit Profile option click listener
        val editprof = findViewById<RelativeLayout>(R.id.editProfilebtn)
        editprof.setOnClickListener {
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

        // Make profile image clickable to edit
        profileImage.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData() // Refresh data when returning from EditProfile
    }

    companion object {
        private const val BASE_URL = "http://192.168.18.37/medque_app"
    }
}
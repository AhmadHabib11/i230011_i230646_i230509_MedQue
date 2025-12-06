package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codeclinic.i230011_i230646_i230509_medque.api.RetrofitClient
import com.codeclinic.i230011_i230646_i230509_medque.models.ApiResponse
import com.codeclinic.i230011_i230646_i230509_medque.models.Doctor
import com.codeclinic.i230011_i230646_i230509_medque.models.DoctorDetailData
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Doctor_detail : AppCompatActivity() {
    
    private lateinit var doctorImage: ImageView
    private lateinit var doctorName: TextView
    private lateinit var doctorSpecialization: TextView
    private lateinit var doctorLocation: TextView
    private lateinit var patientsCount: TextView
    private lateinit var experienceYears: TextView
    private lateinit var doctorRating: TextView
    private lateinit var reviewsCount: TextView
    private lateinit var doctorAbout: TextView
    private lateinit var workingTime: TextView
    
    private var doctorId: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_detail)

        // Get doctor ID from intent
        doctorId = intent.getIntExtra("doctor_id", 0)
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
        
        // Load doctor details
        if (doctorId > 0) {
            loadDoctorDetails()
        } else {
            Toast.makeText(this, "Invalid doctor ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun initializeViews() {
        doctorImage = findViewById(R.id.doctor_image)
        doctorName = findViewById(R.id.doctor_name)
        doctorSpecialization = findViewById(R.id.doctor_specialization)
        doctorLocation = findViewById(R.id.doctor_location)
        patientsCount = findViewById(R.id.patients_count)
        experienceYears = findViewById(R.id.experience_years)
        doctorRating = findViewById(R.id.doctor_rating)
        reviewsCount = findViewById(R.id.reviews_count)
        doctorAbout = findViewById(R.id.doctor_about)
        workingTime = findViewById(R.id.working_time)
    }
    
    private fun setupClickListeners() {
        val back = findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener {
            finish()
        }

        val book = findViewById<TextView>(R.id.btn_book_appointment)
        book.setOnClickListener {
            val intent = Intent(this, Book_appointment::class.java)
            intent.putExtra("doctor_id", doctorId)
            intent.putExtra("doctor_name", doctorName.text.toString())
            startActivity(intent)
        }
    }
    
    private fun loadDoctorDetails() {
        RetrofitClient.apiService.getDoctorDetails(doctorId).enqueue(object : Callback<ApiResponse<DoctorDetailData>> {
            override fun onResponse(
                call: Call<ApiResponse<DoctorDetailData>>,
                response: Response<ApiResponse<DoctorDetailData>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val doctor = response.body()?.data?.doctor
                    if (doctor != null) {
                        displayDoctorDetails(doctor)
                    } else {
                        showError("Doctor details not found")
                    }
                } else {
                    showError("Failed to load doctor details")
                }
            }
            
            override fun onFailure(call: Call<ApiResponse<DoctorDetailData>>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }
    
    private fun displayDoctorDetails(doctor: Doctor) {
        // Set doctor name and basic info
        doctorName.text = doctor.doctor_name
        doctorSpecialization.text = doctor.specialization
        doctorLocation.text = doctor.location ?: "Location not specified"
        
        // Set stats
        patientsCount.text = if (doctor.patients_count > 1000) {
            "${doctor.patients_count / 1000}K+"
        } else {
            doctor.patients_count.toString()
        }
        
        experienceYears.text = "${doctor.experience_years}+"
        doctorRating.text = String.format("%.1f", doctor.rating)
        
        reviewsCount.text = if (doctor.reviews_count > 1000) {
            "${doctor.reviews_count / 1000}K+"
        } else {
            doctor.reviews_count.toString()
        }
        
        // Set about and working time
        doctorAbout.text = if (!doctor.about_me.isNullOrEmpty()) {
            doctor.about_me
        } else {
            "No information available"
        }
        
        workingTime.text = doctor.working_time ?: "Monday-Friday, 08.00 AM - 6.00 PM"
        
        // Load doctor image
        if (!doctor.profile_picture.isNullOrEmpty()) {
            val imageUrl = "http://192.168.18.37/medque_app/uploads/${doctor.profile_picture}"
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.doctor1)
                .error(R.drawable.doctor1)
                .into(doctorImage)
        } else {
            doctorImage.setImageResource(R.drawable.doctor1)
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
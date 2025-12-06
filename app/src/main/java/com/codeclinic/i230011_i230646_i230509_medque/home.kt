package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codeclinic.i230011_i230646_i230509_medque.api.RetrofitClient
import com.codeclinic.i230011_i230646_i230509_medque.models.ApiResponse
import com.codeclinic.i230011_i230646_i230509_medque.models.AppointmentsData
import com.codeclinic.i230011_i230646_i230509_medque.models.AppointmentWithDoctor
import com.codeclinic.i230011_i230646_i230509_medque.models.PatientProfileData
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userName: TextView
    private lateinit var joinedYear: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var appointmentCard: CardView
    private lateinit var appointmentTime: TextView
    private lateinit var doctorName: TextView
    private lateinit var clinicName: TextView
    private lateinit var clinicImage: ImageView
    private lateinit var queueBtn: TextView
    private lateinit var noAppointmentsText: TextView
    
    private var patientId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        // Check if user is logged in
        checkLoginStatus()
        
        // Initialize views
        initializeViews()
        
        // Load user profile
        loadUserProfile()
        
        // Load appointments
        loadAppointments()

        // Handle edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.act_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigationButtons()
    }
    
    private fun initializeViews() {
        userName = findViewById(R.id.user_name)
        joinedYear = findViewById(R.id.joined_year)
        profileImage = findViewById(R.id.profile_image)
        appointmentCard = findViewById(R.id.appointment_card)
        appointmentTime = findViewById(R.id.appointment_time)
        doctorName = findViewById(R.id.doctor_name)
        clinicName = findViewById(R.id.clinic_name)
        clinicImage = findViewById(R.id.clinic_image)
        queueBtn = findViewById(R.id.queue_btn)
        noAppointmentsText = findViewById(R.id.no_appointments_text)
    }
    
    private fun setupNavigationButtons() {
        val uploadReportBtn = findViewById<TextView>(R.id.upload_report_btn)
        uploadReportBtn.setOnClickListener {
            val intent = Intent(this, Upload_reports::class.java)
            startActivity(intent)
        }

        val searchdoct = findViewById<ImageView>(R.id.search_doc)
        searchdoct.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
        }
        
        val navcalender = findViewById<ImageView>(R.id.calenderbtn)
        navcalender.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
        }
        
        val bookbtn = findViewById<TextView>(R.id.btnAll)
        bookbtn.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
        }

        val notifbtn = findViewById<ImageView>(R.id.notification_icon)
        notifbtn.setOnClickListener {
            val intent = Intent(this, Notifications::class.java)
            startActivity(intent)
        }

        val prof = findViewById<ImageView>(R.id.personalbtn)
        prof.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadUserProfile() {
        // Get user ID from SharedPreferences
        val userId = sharedPreferences.getInt("userId", 1) // Default to 1 for now
        
        // Fetch patient profile from API
        RetrofitClient.apiService.getPatientProfile(userId).enqueue(object : Callback<ApiResponse<PatientProfileData>> {
            override fun onResponse(
                call: Call<ApiResponse<PatientProfileData>>,
                response: Response<ApiResponse<PatientProfileData>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val patient = response.body()?.data?.patient
                    if (patient != null) {
                        // Display patient name
                        userName.text = patient.name ?: patient.nickname ?: "User"
                        
                        // Display joined year
                        joinedYear.text = "Joined ${patient.joined_year}"
                        
                        // Load profile picture if available
                        if (!patient.profile_picture.isNullOrEmpty()) {
                            val imageUrl = "http://192.168.1.2/medque_app/uploads/${patient.profile_picture}"
                            Picasso.get()
                                .load(imageUrl)
                                .placeholder(R.drawable.profile_holder)
                                .error(R.drawable.profile_holder)
                                .into(profileImage)
                        }
                        
                        // Save to SharedPreferences for future use
                        with(sharedPreferences.edit()) {
                            putString("userName", patient.name ?: patient.nickname)
                            putString("joinedYear", "Joined ${patient.joined_year}")
                            apply()
                        }
                    }
                } else {
                    // Fallback to SharedPreferences if API fails
                    loadUserProfileFromPrefs()
                }
            }
            
            override fun onFailure(call: Call<ApiResponse<PatientProfileData>>, t: Throwable) {
                // Fallback to SharedPreferences if API fails
                loadUserProfileFromPrefs()
            }
        })
    }
    
    private fun loadUserProfileFromPrefs() {
        // Get user data from SharedPreferences
        val name = sharedPreferences.getString("userName", "User")
        val joinedYearText = sharedPreferences.getString("joinedYear", "Joined 2024")
        
        userName.text = name
        joinedYear.text = joinedYearText
    }
    
    private fun loadAppointments() {
        // Get patient ID from SharedPreferences
        patientId = sharedPreferences.getInt("userId", 1) // Default to 1 for now
        
        RetrofitClient.apiService.getAppointments(patientId).enqueue(object : Callback<ApiResponse<AppointmentsData>> {
            override fun onResponse(
                call: Call<ApiResponse<AppointmentsData>>,
                response: Response<ApiResponse<AppointmentsData>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val appointmentsData = response.body()?.data
                    if (appointmentsData != null && appointmentsData.appointments.isNotEmpty()) {
                        displayAppointment(appointmentsData.appointments[0]) // Show first upcoming appointment
                    } else {
                        showNoAppointments()
                    }
                } else {
                    showNoAppointments()
                }
            }
            
            override fun onFailure(call: Call<ApiResponse<AppointmentsData>>, t: Throwable) {
                showNoAppointments()
                Toast.makeText(this@Home, "Error loading appointments", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun displayAppointment(appointment: AppointmentWithDoctor) {
        appointmentCard.visibility = View.VISIBLE
        noAppointmentsText.visibility = View.GONE
        
        // Format date and time
        val dateTimeText = formatAppointmentDateTime(appointment.appointment_date, appointment.appointment_time)
        appointmentTime.text = dateTimeText
        
        // Set doctor name and specialization
        doctorName.text = "Checkup with ${appointment.doctor_name}"
        clinicName.text = appointment.specialization
        
        // Hide queue button for now (can be implemented later)
        queueBtn.visibility = View.GONE
        
        // Load doctor image
        if (!appointment.profile_picture.isNullOrEmpty()) {
            val imageUrl = "http://192.168.1.2/medque_app/uploads/${appointment.profile_picture}"
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.img)
                .error(R.drawable.img)
                .into(clinicImage)
        } else {
            clinicImage.setImageResource(R.drawable.img)
        }
        
        // Set click listener to view appointment details
        appointmentCard.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
        }
    }
    
    private fun showNoAppointments() {
        appointmentCard.visibility = View.GONE
        noAppointmentsText.visibility = View.VISIBLE
    }
    
    private fun formatAppointmentDateTime(dateString: String, timeString: String): String {
        return try {
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val inputTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            
            val date = inputDateFormat.parse(dateString)
            val time = inputTimeFormat.parse(timeString)
            
            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            calendar.time = date ?: Date()
            
            val dateText = when {
                calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
                
                calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1 -> "Tomorrow"
                
                else -> {
                    val outputDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                    outputDateFormat.format(date ?: Date())
                }
            }
            
            val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeText = outputTimeFormat.format(time ?: Date())
            
            "$dateText, $timeText"
        } catch (e: Exception) {
            "$dateString, $timeString"
        }
    }

    private fun checkLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (!isLoggedIn) {
            // User is not logged in, redirect to Signin
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }
    }
}
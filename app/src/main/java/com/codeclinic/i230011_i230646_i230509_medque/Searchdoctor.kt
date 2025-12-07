package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codeclinic.i230011_i230646_i230509_medque.adapters.DoctorAdapter
import com.codeclinic.i230011_i230646_i230509_medque.api.RetrofitClient
import com.codeclinic.i230011_i230646_i230509_medque.models.ApiResponse
import com.codeclinic.i230011_i230646_i230509_medque.models.Doctor
import com.codeclinic.i230011_i230646_i230509_medque.models.DoctorsData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Searchdoctor : AppCompatActivity() {
    
    private lateinit var searchEditText: EditText
    private lateinit var doctorsRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var resultsCountText: TextView
    private lateinit var doctorAdapter: DoctorAdapter
    private lateinit var specialtyButtonsContainer: LinearLayout
    private var allDoctors: List<Doctor> = emptyList()
    private var currentSpecialization: String? = null
    private var searchQuery: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_searchdoctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize views
        searchEditText = findViewById(R.id.searchEditText)
        doctorsRecyclerView = findViewById(R.id.doctorsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        resultsCountText = findViewById(R.id.resultsCountText)
        specialtyButtonsContainer = findViewById(R.id.specialtyButtonsContainer)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup search functionality
        setupSearchBar()
        
        // Setup navigation buttons
        setupNavigationButtons()
        
        // Setup specialty filter buttons
        setupSpecialtyFilters()
        
        // Load all doctors
        loadDoctors()
    }
    
    private fun setupSearchBar() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterDoctors()
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupRecyclerView() {
        doctorAdapter = DoctorAdapter(emptyList()) { doctor ->
            // Handle doctor click - navigate to doctor detail
            val intent = Intent(this, Doctor_detail::class.java)
            intent.putExtra("doctor_id", doctor.id)
            intent.putExtra("doctor_name", doctor.doctor_name)
            intent.putExtra("specialization", doctor.specialization)
            startActivity(intent)
        }
        
        doctorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@Searchdoctor)
            adapter = doctorAdapter
        }
    }
    
    private fun setupNavigationButtons() {
        val back_btn = findViewById<ImageView>(R.id.btnBack)
        back_btn.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
            finish()
        }
        
        val home_btn = findViewById<ImageView>(R.id.homebt)
        home_btn.setOnClickListener {
            val intent = Intent(this, home::class.java)
            startActivity(intent)
            finish()
        }
        
        val navcalender = findViewById<ImageView>(R.id.calendarbt)
        navcalender.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
            finish()
        }
        
        val prof = findViewById<ImageView>(R.id.profilebt)
        prof.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun setupSpecialtyFilters() {
        // This will be called after doctors are loaded
        // Initial setup is empty, buttons will be created dynamically
    }
    
    private fun createDynamicSpecialtyButtons() {
        specialtyButtonsContainer.removeAllViews()
        
        // Get unique specializations from all doctors
        val specializations = allDoctors.map { it.specialization }.distinct().sorted()
        
        // Create "All" button
        val allBtn = createSpecialtyButton("All", null, true)
        specialtyButtonsContainer.addView(allBtn)
        
        // Create buttons for each specialization found in database
        specializations.forEach { specialization ->
            val button = createSpecialtyButton(specialization, specialization, false)
            specialtyButtonsContainer.addView(button)
        }
    }
    
    private fun createSpecialtyButton(text: String, specialty: String?, isSelected: Boolean): TextView {
        val button = TextView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        // Add margin for buttons after the first one
        if (specialtyButtonsContainer.childCount > 0) {
            layoutParams.setMargins(dpToPx(12), 0, 0, 0)
        }
        
        button.layoutParams = layoutParams
        button.text = text
        button.setPadding(dpToPx(28), dpToPx(12), dpToPx(28), dpToPx(12))
        button.textSize = 16f
        button.setTypeface(null, android.graphics.Typeface.BOLD)
        
        // Set initial style
        if (isSelected) {
            button.setBackgroundResource(R.drawable.selected)
            button.setTextColor(getColor(R.color.white))
        } else {
            button.setBackgroundResource(R.drawable.unselected)
            button.setTextColor(getColor(R.color.primary_green))
        }
        
        // Set click listener
        button.setOnClickListener {
            currentSpecialization = specialty
            filterDoctors()
            updateDynamicButtonStyles(button)
        }
        
        return button
    }
    
    private fun updateDynamicButtonStyles(selectedButton: TextView) {
        for (i in 0 until specialtyButtonsContainer.childCount) {
            val button = specialtyButtonsContainer.getChildAt(i) as TextView
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.selected)
                button.setTextColor(getColor(R.color.white))
            } else {
                button.setBackgroundResource(R.drawable.unselected)
                button.setTextColor(getColor(R.color.primary_green))
            }
        }
    }
    
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    private fun loadDoctors() {
        progressBar.visibility = View.VISIBLE
        doctorsRecyclerView.visibility = View.GONE
        emptyStateText.visibility = View.GONE
        
        RetrofitClient.apiService.getAllDoctors().enqueue(object : Callback<ApiResponse<DoctorsData>> {
            override fun onResponse(
                call: Call<ApiResponse<DoctorsData>>,
                response: Response<ApiResponse<DoctorsData>>
            ) {
                progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val doctorsData = response.body()?.data
                    if (doctorsData != null && doctorsData.doctors.isNotEmpty()) {
                        allDoctors = doctorsData.doctors
                        createDynamicSpecialtyButtons()
                        filterDoctors()
                        doctorsRecyclerView.visibility = View.VISIBLE
                    } else {
                        showEmptyState()
                    }
                } else {
                    showEmptyState()
                    Toast.makeText(this@Searchdoctor, "Failed to load doctors", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<ApiResponse<DoctorsData>>, t: Throwable) {
                progressBar.visibility = View.GONE
                showEmptyState()
                Toast.makeText(
                    this@Searchdoctor, 
                    "Error: ${t.message}", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    
    private fun filterDoctors() {
        var filteredDoctors = if (currentSpecialization == null) {
            allDoctors
        } else {
            allDoctors.filter { it.specialization.equals(currentSpecialization, ignoreCase = true) }
        }
        
        // Apply search query filter
        if (searchQuery.isNotEmpty()) {
            filteredDoctors = filteredDoctors.filter { doctor ->
                doctor.doctor_name.contains(searchQuery, ignoreCase = true) ||
                doctor.specialization.contains(searchQuery, ignoreCase = true) ||
                doctor.location?.contains(searchQuery, ignoreCase = true) == true
            }
        }
        
        // Update results count
        resultsCountText.text = "${filteredDoctors.size} found"
        
        if (filteredDoctors.isEmpty()) {
            showEmptyState()
        } else {
            doctorAdapter.updateDoctors(filteredDoctors)
            doctorsRecyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }
    
    private fun showEmptyState() {
        doctorsRecyclerView.visibility = View.GONE
        emptyStateText.visibility = View.VISIBLE
    }
}
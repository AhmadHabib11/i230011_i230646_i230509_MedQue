package com.codeclinic.i230011_i230646_i230509_medque

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.codeclinic.i230011_i230646_i230509_medque.utils.FirebaseRealtimeHelper
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EditProfile : AppCompatActivity() {

    private lateinit var genderLayout: RelativeLayout
    private lateinit var tvGender: TextView
    private lateinit var btnBack: ImageView
    private lateinit var dobLayout: RelativeLayout
    private lateinit var tvDateOfBirth: TextView
    private lateinit var etFullName: EditText
    private lateinit var etNickname: EditText
    private lateinit var etEmail: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue

    private val calendar = Calendar.getInstance()
    private var selectedDate: String? = null
    private var selectedImageUri: Uri? = null

    private val BASE_URL = "http://192.168.1.4/medque_app"

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.dp_circle)
                .error(R.drawable.dp_circle)
                .into(profileImageView)

            // Get user info
            val userId = sharedPreferences.getInt("user_id", -1)
            val userRole = sharedPreferences.getString("role", "")
            val isDoctor = userRole == "doctor"

            // Save to Firebase Realtime Database first
            val firebaseHelper = FirebaseRealtimeHelper()
            firebaseHelper.saveProfileImageBase64(
                this@EditProfile,
                it,
                userId,
                isDoctor
            ) { success: Boolean, message: String, base64String: String? ->
                if (success && base64String != null) {
                    // Then upload to MySQL via PHP with Base64
                    uploadImageToMySQL(userId, base64String) { mysqlSuccess, mysqlMessage, imagePath ->
                        if (mysqlSuccess && imagePath != null) {
                            // Save image path to SharedPreferences
                            with(sharedPreferences.edit()) {
                                putString("profile_picture", imagePath)
                                apply()
                            }
                            Toast.makeText(this@EditProfile, "Profile picture updated in both Firebase and MySQL", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@EditProfile, "MySQL upload failed: $mysqlMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@EditProfile, "Firebase save failed: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.editprofile)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)
        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)

        val savebtn = findViewById<TextView>(R.id.btnSave)
        savebtn.setOnClickListener {
            updateProfileWithVolley()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_screen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
        loadUserData()
    }

    private fun initializeViews() {
        genderLayout = findViewById(R.id.genderLayout)
        tvGender = findViewById(R.id.tvGender)
        btnBack = findViewById(R.id.btnBack)
        dobLayout = findViewById(R.id.dobLayout)
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
        etFullName = findViewById(R.id.etFullName)
        etNickname = findViewById(R.id.etNickname)
        etEmail = findViewById(R.id.etEmail)
        profileImageView = findViewById(R.id.outerCircle)

        // Make profile image clickable
        findViewById<RelativeLayout>(R.id.editPhotoButton).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupListeners() {
        // Gender selection
        genderLayout.setOnClickListener {
            showGenderPopup(it)
        }

        // Date of birth selection
        dobLayout.setOnClickListener {
            showDatePicker()
        }

        // Back button
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val name = sharedPreferences.getString("name", "")
        val nickname = sharedPreferences.getString("nickname", "")
        val email = sharedPreferences.getString("email", "")
        val dob = sharedPreferences.getString("dob", "")
        val gender = sharedPreferences.getString("gender", "")
        val profilePicture = sharedPreferences.getString("profile_picture", "")

        // Set values
        etFullName.setText(name)
        etNickname.setText(nickname)
        etEmail.setText(email)

        if (!dob.isNullOrEmpty()) {
            // Format date from YYYY-MM-DD to DD/MM/YYYY
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dob)
                if (date != null) {
                    val formattedDate = outputFormat.format(date)
                    tvDateOfBirth.text = formattedDate
                    tvDateOfBirth.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                    selectedDate = dob
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!gender.isNullOrEmpty()) {
            tvGender.text = gender
            tvGender.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        }

        // Load profile image using Picasso
        if (!profilePicture.isNullOrEmpty()) {
            Picasso.get()
                .load("$BASE_URL/uploads/$profilePicture")
                .placeholder(R.drawable.dp_circle)
                .error(R.drawable.dp_circle)
                .into(profileImageView)
        }
    }

    private fun showGenderPopup(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Male")
        popup.menu.add(0, 2, 1, "Female")
        popup.menu.add(0, 3, 2, "Other")
        popup.menu.add(0, 4, 3, "Prefer not to say")

        popup.setOnMenuItemClickListener { item ->
            val gender = when (item.itemId) {
                1 -> "Male"
                2 -> "Female"
                3 -> "Other"
                4 -> "Prefer not to say"
                else -> ""
            }
            tvGender.text = gender
            tvGender.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            true
        }

        popup.show()
    }

    private fun showDatePicker() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Update calendar with selected date
                calendar.set(year, month, dayOfMonth)

                // Format for display (DD/MM/YYYY)
                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = displayFormat.format(calendar.time)

                // Format for database (YYYY-MM-DD)
                val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dbFormat.format(calendar.time)

                tvDateOfBirth.text = formattedDate
                tvDateOfBirth.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            },
            currentYear,
            currentMonth,
            currentDay
        )

        // Set maximum date to today
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Set minimum date (100 years ago)
        val minCalendar = Calendar.getInstance()
        minCalendar.set(currentYear - 100, 0, 1)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        datePickerDialog.show()
    }

    private fun updateProfileWithVolley() {
        val name = etFullName.text.toString().trim()
        val nickname = etNickname.text.toString().trim()
        val gender = tvGender.text.toString()
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val savebtn = findViewById<TextView>(R.id.btnSave)
        savebtn.isEnabled = false
        savebtn.text = "Saving..."

        val url = "$BASE_URL/update_profile.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("name", name)
            put("nickname", nickname)
            if (selectedDate != null) put("dob", selectedDate)
            put("gender", gender)
            val profilePicture = sharedPreferences.getString("profile_picture", null)
            if (profilePicture != null) put("profile_picture", profilePicture)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                savebtn.isEnabled = true
                savebtn.text = "Save"

                val success = response.getBoolean("success")
                val message = response.getString("message")

                if (success) {
                    // Update SharedPreferences with new data
                    with(sharedPreferences.edit()) {
                        putString("name", name)
                        putString("nickname", nickname)
                        if (selectedDate != null) putString("dob", selectedDate)
                        putString("gender", gender)
                        apply()
                    }

                    Toast.makeText(this@EditProfile, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditProfile, message, Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                savebtn.isEnabled = true
                savebtn.text = "Save"

                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                Toast.makeText(this@EditProfile, "Network error: $errorMessage", Toast.LENGTH_LONG).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun uploadImageToMySQL(userId: Int, base64Image: String, callback: (Boolean, String, String?) -> Unit) {
        val url = "$BASE_URL/upload_image.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.getString("message")
                    val imagePath = if (success && jsonResponse.has("data")) {
                        jsonResponse.getJSONObject("data").getString("filename")
                    } else null
                    callback(success, message, imagePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(false, "Failed to parse response: ${e.message}", null)
                }
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                callback(false, errorMessage, null)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                params["image_base64"] = base64Image
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
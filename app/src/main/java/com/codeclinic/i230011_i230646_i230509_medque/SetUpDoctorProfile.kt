package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class SetUpDoctorProfile : AppCompatActivity() {

    private val BASE_URL = "http://192.168.1.2/medque_app"
    private var userId: Int = -1
    private var selectedImageUri: Uri? = null
    private lateinit var profileImageView: ImageView
    private lateinit var requestQueue: com.android.volley.RequestQueue

    private val specializations = arrayOf(
        "Cardiologist",
        "Dermatologist",
        "Neurologist",
        "Pediatrician",
        "Psychiatrist",
        "Orthopedist",
        "Ophthalmologist",
        "ENT Specialist",
        "General Physician",
        "Other"
    )

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Log.d("DoctorProfile", "Image selected: $it")
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.dp_circle)
                .error(R.drawable.dp_circle)
                .into(profileImageView)
            Toast.makeText(this, "Image selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DoctorProfile", "onCreate called")
        setContentView(R.layout.setup_doctor_profile)

        requestQueue = Volley.newRequestQueue(this)

        userId = intent.getIntExtra("user_id", -1)
        val isEditing = intent.getBooleanExtra("is_editing", false)
        Log.d("DoctorProfile", "User ID: $userId, Editing: $isEditing")

        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            val nameInput = findViewById<EditText>(R.id.etDoctorName)
            val specializationInput = findViewById<EditText>(R.id.etSpecialization)
            val experienceInput = findViewById<EditText>(R.id.etExperience)
            val workingHoursInput = findViewById<EditText>(R.id.etWorkingHours)
            val locationInput = findViewById<EditText>(R.id.etLocation)
            val aboutMeInput = findViewById<EditText>(R.id.etAboutMe)
            val savebtn = findViewById<Button>(R.id.btnSave)
            val backarr = findViewById<ImageView>(R.id.btnBack)
            profileImageView = findViewById(R.id.ivProfileImage)
            val editImageBtn = findViewById<ImageView>(R.id.btnEditImage)
            val profileCard = findViewById<CardView>(R.id.cvProfileImage)
            val titleText = findViewById<TextView>(R.id.titleText)

            // Set title based on mode
            if (isEditing) {
                titleText.text = "Edit Doctor Profile"
                savebtn.text = "Update Profile"
                // Load existing data
                loadExistingDoctorData()
            }

            Log.d("DoctorProfile", "All views found successfully")

            // Make the ENTIRE card clickable for image selection
            profileCard.setOnClickListener {
                Log.d("DoctorProfile", "Profile card clicked - launching image picker")
                imagePickerLauncher.launch("image/*")
            }

            // Also make the camera icon clickable
            editImageBtn.setOnClickListener {
                Log.d("DoctorProfile", "Camera icon clicked - launching image picker")
                imagePickerLauncher.launch("image/*")
            }

            // Make the image itself clickable
            profileImageView.setOnClickListener {
                Log.d("DoctorProfile", "Profile image clicked - launching image picker")
                imagePickerLauncher.launch("image/*")
            }

            // Specialization dropdown
            specializationInput.setOnClickListener {
                Log.d("DoctorProfile", "Specialization clicked - showing dialog")
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Select Specialization")
                builder.setItems(specializations) { dialog, which ->
                    specializationInput.setText(specializations[which])
                    Log.d("DoctorProfile", "Selected: ${specializations[which]}")
                    dialog.dismiss()
                }
                builder.show()
            }

            savebtn.setOnClickListener {
                Log.d("DoctorProfile", "Save button clicked")
                val name = nameInput.text.toString().trim()
                val specialization = specializationInput.text.toString().trim()
                val experience = experienceInput.text.toString().trim()
                val workingHours = workingHoursInput.text.toString().trim()
                val location = locationInput.text.toString().trim()
                val aboutMe = aboutMeInput.text.toString().trim()

                Log.d("DoctorProfile", "Name: '$name', Spec: '$specialization', Exp: '$experience'")

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (specialization.isEmpty()) {
                    Toast.makeText(this, "Please select specialization", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (experience.isEmpty()) {
                    Toast.makeText(this, "Please enter years of experience", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (workingHours.isEmpty()) {
                    Toast.makeText(this, "Please enter working hours", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (location.isEmpty()) {
                    Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                savebtn.isEnabled = false
                savebtn.text = if (isEditing) "Updating..." else "Saving..."

                if (selectedImageUri != null) {
                    Log.d("DoctorProfile", "Uploading image first")
                    uploadImageWithVolley(selectedImageUri!!) { imageSuccess, imageMessage, imagePath ->
                        if (imageSuccess) {
                            Log.d("DoctorProfile", "Image uploaded: $imagePath")
                            createDoctorProfileWithVolley(name, specialization, experience.toIntOrNull() ?: 0,
                                workingHours, location, aboutMe, imagePath, isEditing) { success, message ->
                                handleSaveResponse(savebtn, success, message, isEditing)
                            }
                        } else {
                            Log.e("DoctorProfile", "Image upload failed: $imageMessage")
                            Toast.makeText(this, "Image upload failed, continuing without image", Toast.LENGTH_SHORT).show()
                            createDoctorProfileWithVolley(name, specialization, experience.toIntOrNull() ?: 0,
                                workingHours, location, aboutMe, null, isEditing) { success, message ->
                                handleSaveResponse(savebtn, success, message, isEditing)
                            }
                        }
                    }
                } else {
                    Log.d("DoctorProfile", "No image selected, saving without image")
                    createDoctorProfileWithVolley(name, specialization, experience.toIntOrNull() ?: 0,
                        workingHours, location, aboutMe, null, isEditing) { success, message ->
                        handleSaveResponse(savebtn, success, message, isEditing)
                    }
                }
            }

            backarr.setOnClickListener {
                Log.d("DoctorProfile", "Back button clicked")
                android.app.AlertDialog.Builder(this)
                    .setTitle("Cancel ${if (isEditing) "Update" else "Setup"}?")
                    .setMessage("Are you sure you want to cancel? Your progress will be lost.")
                    .setPositiveButton("Yes") { _, _ ->
                        if (isEditing) {
                            finish() // Go back to DoctorHome
                        } else {
                            finish() // Go back to Signup
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        } catch (e: Exception) {
            Log.e("DoctorProfile", "Error initializing views: ${e.message}", e)
            Toast.makeText(this, "Error loading screen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Update the loadExistingDoctorData() function:

    private fun loadExistingDoctorData() {
        val url = "$BASE_URL/get_doctor_profile.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("DoctorProfile", "Loading existing data for user_id: $userId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    Log.d("DoctorProfile", "Existing data response: $response")
                    if (response.getBoolean("success")) {
                        val doctorData = response.getJSONObject("data")

                        // ✅ Populate all fields with existing data
                        runOnUiThread {
                            findViewById<EditText>(R.id.etDoctorName).setText(doctorData.getString("doctor_name"))
                            findViewById<EditText>(R.id.etSpecialization).setText(doctorData.getString("specialization"))

                            val experience = doctorData.optInt("experience_years", 0)
                            findViewById<EditText>(R.id.etExperience).setText(experience.toString())

                            val workingHours = doctorData.optString("working_time", "")
                            findViewById<EditText>(R.id.etWorkingHours).setText(workingHours)

                            val location = doctorData.optString("location", "")
                            findViewById<EditText>(R.id.etLocation).setText(location)

                            val aboutMe = doctorData.optString("about_me", "")
                            findViewById<EditText>(R.id.etAboutMe).setText(aboutMe)

                            val profilePicture = doctorData.optString("profile_picture", "")
                            if (profilePicture.isNotEmpty() && profilePicture != "null") {
                                val imageUrl = "$BASE_URL/uploads/$profilePicture"
                                Log.d("DoctorProfile", "Loading existing image: $imageUrl")
                                Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(R.drawable.dp_circle)
                                    .error(R.drawable.dp_circle)
                                    .into(profileImageView)
                            }
                        }

                        Log.d("DoctorProfile", "Existing data loaded successfully")
                    } else {
                        val message = response.getString("message")
                        Log.e("DoctorProfile", "Error loading existing data: $message")
                        Toast.makeText(this, "Error loading profile: $message", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("DoctorProfile", "Error parsing existing data: ${e.message}")
                    Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("DoctorProfile", "Error loading doctor data: ${error.message}")
                Toast.makeText(this, "Network error loading profile", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun handleSaveResponse(savebtn: Button, success: Boolean, message: String, isEditing: Boolean) {
        runOnUiThread {
            savebtn.isEnabled = true
            savebtn.text = if (isEditing) "Update Profile" else "Save Profile"

            if (success) {
                val sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("user_id", userId)
                    putString("user_type", "doctor")

                    // ✅ CRITICAL FIX: Always set profile_completed to true for doctors
                    putBoolean("profile_completed", true)

                    // For new signup, set isLoggedIn to false (needs login)
                    // For editing, keep isLoggedIn as true
                    if (!isEditing) {
                        putBoolean("isLoggedIn", false)
                    } else {
                        putBoolean("isLoggedIn", true)
                    }
                    apply()
                }

                Toast.makeText(this,
                    if (isEditing) "✅ Profile updated successfully!"
                    else "✅ Profile created successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                if (isEditing) {
                    // Go back to DoctorHome
                    val intent = Intent(this, DoctorHome::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Navigate to ProfileSetUpSuccess
                    val intent = Intent(this, ProfileSetUpSuccess::class.java)
                    intent.putExtra("user_id", userId)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImageWithVolley(imageUri: Uri, callback: (Boolean, String, String?) -> Unit) {
        val url = "$BASE_URL/upload_doctor_image.php"
        Log.d("DoctorProfile", "Uploading image to: $url")

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    Log.d("DoctorProfile", "Image upload response: $response")
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.getString("message")
                    val imagePath = if (success && jsonResponse.has("data")) {
                        jsonResponse.getJSONObject("data").getString("filename")
                    } else null
                    callback(success, message, imagePath)
                } catch (e: Exception) {
                    Log.e("DoctorProfile", "Error parsing image response: ${e.message}")
                    callback(false, "Failed to parse response", null)
                }
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                Log.e("DoctorProfile", "Image upload error: $errorMessage")
                callback(false, errorMessage, null)
            }
        ) {
            override fun getBodyContentType(): String {
                return "multipart/form-data;boundary=$boundary"
            }

            override fun getBody(): ByteArray {
                return createImageRequestBody(imageUri)
            }

            private val boundary = "Boundary-${System.currentTimeMillis()}"

            private fun createImageRequestBody(imageUri: Uri): ByteArray {
                val outputStream = ByteArrayOutputStream()

                outputStream.write("--$boundary\r\n".toByteArray())
                outputStream.write("Content-Disposition: form-data; name=\"user_id\"\r\n\r\n".toByteArray())
                outputStream.write("$userId\r\n".toByteArray())

                val fileName = "doctor_${userId}_${System.currentTimeMillis()}.jpg"
                outputStream.write("--$boundary\r\n".toByteArray())
                outputStream.write("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"\r\n".toByteArray())
                outputStream.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())

                val inputStream = contentResolver.openInputStream(imageUri)
                inputStream?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }

                outputStream.write("\r\n".toByteArray())
                outputStream.write("--$boundary--\r\n".toByteArray())

                return outputStream.toByteArray()
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun createDoctorProfileWithVolley(
        name: String,
        specialization: String,
        experienceYears: Int,
        workingTime: String,
        location: String,
        aboutMe: String,
        profilePicture: String?,
        isUpdating: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        val url = "$BASE_URL/create_doctor_profile.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("doctor_name", name)
            put("specialization", specialization)
            put("experience_years", experienceYears)
            put("working_time", workingTime)
            put("location", location)
            put("about_me", aboutMe)
            if (profilePicture != null) put("profile_picture", profilePicture)
            put("is_update", isUpdating) // Tell backend if this is an update
        }

        Log.d("DoctorProfile", "Creating/updating profile with: $jsonObject")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("DoctorProfile", "Profile creation response: $response")
                val success = response.getBoolean("success")
                val message = response.getString("message")
                callback(success, message)
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                Log.e("DoctorProfile", "Profile creation error: $errorMessage")
                callback(false, errorMessage)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
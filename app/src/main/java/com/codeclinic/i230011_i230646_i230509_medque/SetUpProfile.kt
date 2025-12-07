package com.codeclinic.i230011_i230646_i230509_medque

import android.app.DatePickerDialog
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
import java.util.*

class SetUpProfile : AppCompatActivity() {

    private val BASE_URL = "http://192.168.1.4/medque_app"
    private var userId: Int = -1
    private var selectedDate: String? = null
    private var selectedImageUri: Uri? = null
    private lateinit var profileImageView: ImageView
    private lateinit var requestQueue: com.android.volley.RequestQueue

    companion object {
        private const val TAG = "SetUpProfile"
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Log.d(TAG, "Image selected: $it")
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
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.setupprofile)

        requestQueue = Volley.newRequestQueue(this)

        userId = intent.getIntExtra("user_id", -1)
        Log.d(TAG, "User ID: $userId")

        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val nameInput = findViewById<EditText>(R.id.etName)
        val nicknameInput = findViewById<EditText>(R.id.etNickname)
        val dobContainer = findViewById<RelativeLayout>(R.id.dobContainer)
        val dobText = findViewById<TextView>(R.id.tvDateOfBirth)
        val genderInput = findViewById<EditText>(R.id.etGender)
        val savebtn = findViewById<Button>(R.id.btnSave)
        val backarr = findViewById<ImageView>(R.id.btnBack)
        profileImageView = findViewById(R.id.ivProfileImage)
        val editImageBtn = findViewById<ImageView>(R.id.btnEditImage)
        val profileContainer = findViewById<RelativeLayout>(R.id.profileImageContainer)

        // Make profile card clickable
        profileContainer.setOnClickListener {
            Log.d(TAG, "Profile container clicked")
            imagePickerLauncher.launch("image/*")
        }

        editImageBtn.setOnClickListener {
            Log.d(TAG, "Edit image button clicked")
            imagePickerLauncher.launch("image/*")
        }

        profileImageView.setOnClickListener {
            Log.d(TAG, "Profile image clicked")
            imagePickerLauncher.launch("image/*")
        }

        // Date picker for DOB
        dobContainer.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    dobText.text = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    dobText.setTextColor(resources.getColor(android.R.color.black))
                },
                year, month, day
            )
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        // Gender dropdown
        genderInput.setOnClickListener {
            val genders = arrayOf("Male", "Female", "Other", "Prefer not to say")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Select Gender")
            builder.setItems(genders) { _, which ->
                genderInput.setText(genders[which])
            }
            builder.show()
        }

        savebtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val nickname = nicknameInput.text.toString().trim()
            val gender = genderInput.text.toString().trim()

            Log.d(TAG, "Save clicked - Name: '$name', Nickname: '$nickname', Gender: '$gender'")

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            savebtn.isEnabled = false
            savebtn.text = "Saving..."

            // ✅ UPDATED: Upload image first if selected
            if (selectedImageUri != null) {
                Log.d(TAG, "Uploading image first")
                uploadImageWithVolley(selectedImageUri!!) { imageSuccess, imageMessage, imagePath ->
                    if (imageSuccess) {
                        Log.d(TAG, "Image uploaded: $imagePath")
                        createPatientProfileWithVolley(name, nickname, selectedDate, gender, imagePath) { success, message ->
                            handleSaveResponse(savebtn, success, message)
                        }
                    } else {
                        Log.e(TAG, "Image upload failed: $imageMessage")
                        Toast.makeText(this, "Image upload failed, continuing without image", Toast.LENGTH_SHORT).show()
                        createPatientProfileWithVolley(name, nickname, selectedDate, gender, null) { success, message ->
                            handleSaveResponse(savebtn, success, message)
                        }
                    }
                }
            } else {
                Log.d(TAG, "No image selected, saving without image")
                createPatientProfileWithVolley(name, nickname, selectedDate, gender, null) { success, message ->
                    handleSaveResponse(savebtn, success, message)
                }
            }
        }

        backarr.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            android.app.AlertDialog.Builder(this)
                .setTitle("Cancel Profile Setup?")
                .setMessage("Are you sure you want to cancel? Your progress will be lost.")
                .setPositiveButton("Yes") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun handleSaveResponse(savebtn: Button, success: Boolean, message: String) {
        runOnUiThread {
            savebtn.isEnabled = true
            savebtn.text = "Save"

            if (success) {
                val sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putInt("user_id", userId)
                    putString("user_type", "patient")
                    putBoolean("profile_completed", true) // ✅ Profile completed
                    putBoolean("isLoggedIn", false) // ✅ NOT logged in - needs to login
                    apply()
                }

                Toast.makeText(this, "Profile created successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to ProfileSetUpSuccess
                val intent = Intent(this, ProfileSetUpSuccess::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
                finish()
            }
        }
    }

    // ✅ UPDATED: Use new upload_patient_image.php endpoint
    private fun uploadImageWithVolley(imageUri: Uri, callback: (Boolean, String, String?) -> Unit) {
        val url = "$BASE_URL/upload_patient_image.php"
        Log.d(TAG, "Uploading image to: $url")

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    Log.d(TAG, "Image upload response: $response")
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.getString("message")
                    val imagePath = if (success && jsonResponse.has("data")) {
                        jsonResponse.getJSONObject("data").getString("filename")
                    } else null
                    callback(success, message, imagePath)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing image response: ${e.message}")
                    callback(false, "Failed to parse response", null)
                }
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                Log.e(TAG, "Image upload error: $errorMessage")
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

                val fileName = "patient_${userId}_${System.currentTimeMillis()}.jpg"
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

    // ✅ NEW: Use create_patient_profile.php endpoint
    private fun createPatientProfileWithVolley(
        name: String,
        nickname: String,
        dob: String?,
        gender: String,
        profilePicture: String?,
        callback: (Boolean, String) -> Unit
    ) {
        val url = "$BASE_URL/create_patient_profile.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("name", name)
            put("nickname", nickname)
            if (dob != null) put("dob", dob)
            put("gender", gender)
            if (profilePicture != null) put("profile_picture", profilePicture)
        }

        Log.d(TAG, "Creating profile with: $jsonObject")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d(TAG, "Profile creation response: $response")
                val success = response.getBoolean("success")
                val message = response.getString("message")
                callback(success, message)
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                Log.e(TAG, "Profile creation error: $errorMessage")
                callback(false, errorMessage)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
package com.codeclinic.i230011_i230646_i230509_medque

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*

class SetUpProfile : AppCompatActivity() {

    private val BASE_URL = "http://192.168.100.22/medque_app"
    private var userId: Int = -1
    private var selectedDate: String? = null
    private var selectedImageUri: Uri? = null
    private lateinit var profileImageView: ImageView
    private lateinit var requestQueue: com.android.volley.RequestQueue

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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setupprofile)

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(this)

        // Get user ID from intent
        userId = intent.getIntExtra("user_id", -1)
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

        // Image picker
        editImageBtn.setOnClickListener {
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

            // Validate at least name is provided
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button during request
            savebtn.isEnabled = false
            savebtn.text = "Saving..."

            // First upload image if selected, then update profile
            if (selectedImageUri != null) {
                uploadImageWithVolley(selectedImageUri!!) { imageSuccess, imageMessage, imagePath ->
                    if (imageSuccess) {
                        // Image uploaded, now update profile with image path
                        updateProfileWithVolley(name, nickname, selectedDate, gender, imagePath) { success, message ->
                            handleSaveResponse(savebtn, success, message)
                        }
                    } else {
                        // Image upload failed, but continue with profile update
                        Toast.makeText(this, "Image upload failed: $imageMessage", Toast.LENGTH_SHORT).show()
                        updateProfileWithVolley(name, nickname, selectedDate, gender, null) { success, message ->
                            handleSaveResponse(savebtn, success, message)
                        }
                    }
                }
            } else {
                // No image selected, just update profile
                updateProfileWithVolley(name, nickname, selectedDate, gender, null) { success, message ->
                    handleSaveResponse(savebtn, success, message)
                }
            }
        }

        backarr.setOnClickListener {
            finish()
        }
    }

    private fun handleSaveResponse(savebtn: Button, success: Boolean, message: String) {
        runOnUiThread {
            savebtn.isEnabled = true
            savebtn.text = "Save"

            if (success) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProfileSetUpSuccess::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImageWithVolley(imageUri: Uri, callback: (Boolean, String, String?) -> Unit) {
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
                    callback(false, "Failed to parse response", null)
                }
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
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

                // Add user_id parameter
                outputStream.write("--$boundary\r\n".toByteArray())
                outputStream.write("Content-Disposition: form-data; name=\"user_id\"\r\n\r\n".toByteArray())
                outputStream.write("$userId\r\n".toByteArray())

                // Add image file
                val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
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

    private fun updateProfileWithVolley(
        name: String,
        nickname: String,
        dob: String?,
        gender: String,
        profilePicture: String?,
        callback: (Boolean, String) -> Unit
    ) {
        val url = "$BASE_URL/update_profile.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("name", name)
            put("nickname", nickname)
            if (dob != null) put("dob", dob)
            put("gender", gender)
            if (profilePicture != null) put("profile_picture", profilePicture)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                val success = response.getBoolean("success")
                val message = response.getString("message")
                callback(success, message)
            },
            { error ->
                val errorMessage = error.networkResponse?.let {
                    String(it.data, Charsets.UTF_8)
                } ?: error.message ?: "Network error"
                callback(false, errorMessage)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
package com.codeclinic.i230011_i230646_i230509_medque

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class SetUpProfile : AppCompatActivity() {

    // Replace with your server IP address (4 dots format)
    private val BASE_URL = "http://192.168.18.37/medque_app"

    private var userId: Int = -1
    private var selectedDate: String? = null
    private var selectedImageUri: Uri? = null
    private lateinit var profileImageView: ImageView

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setupprofile)

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
                uploadImage(selectedImageUri!!) { imageSuccess, imageMessage, imagePath ->
                    if (imageSuccess) {
                        // Image uploaded, now update profile with image path
                        updateProfile(name, nickname, selectedDate, gender, imagePath) { success, message ->
                            handleSaveResponse(savebtn, success, message)
                        }
                    } else {
                        // Image upload failed, but continue with profile update
                        Toast.makeText(this, "Image upload failed: $imageMessage", Toast.LENGTH_SHORT).show()
                        updateProfile(name, nickname, selectedDate, gender, null) { success, message ->
                            handleSaveResponse(savebtn, success, message)
                        }
                    }
                }
            } else {
                // No image selected, just update profile
                updateProfile(name, nickname, selectedDate, gender, null) { success, message ->
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

    private fun uploadImage(imageUri: Uri, callback: (Boolean, String, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$BASE_URL/upload_image.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 30000
                connection.readTimeout = 30000

                val boundary = "----Boundary${System.currentTimeMillis()}"
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                val outputStream = DataOutputStream(connection.outputStream)

                // Add user_id parameter
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"user_id\"\r\n\r\n")
                outputStream.writeBytes("$userId\r\n")

                // Add image file
                val inputStream = contentResolver.openInputStream(imageUri)
                val fileName = getFileName(imageUri)

                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"\r\n")
                outputStream.writeBytes("Content-Type: image/*\r\n\r\n")

                inputStream?.use { input ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }

                outputStream.writeBytes("\r\n")
                outputStream.writeBytes("--$boundary--\r\n")
                outputStream.flush()
                outputStream.close()

                // Read response
                val responseCode = connection.responseCode
                val reader = if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream))
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream))
                }

                val response = reader.use { it.readText() }
                val jsonResponse = JSONObject(response)

                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")
                val imagePath = if (success && jsonResponse.has("data")) {
                    jsonResponse.getJSONObject("data").getString("filename")
                } else null

                withContext(Dispatchers.Main) {
                    callback(success, message, imagePath)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(false, "Network error: ${e.message}", null)
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "image.jpg"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun updateProfile(
        name: String,
        nickname: String,
        dob: String?,
        gender: String,
        profilePicture: String?,
        callback: (Boolean, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$BASE_URL/update_profile.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                // Create JSON payload
                val jsonPayload = JSONObject().apply {
                    put("user_id", userId)
                    put("name", name)
                    put("nickname", nickname)
                    if (dob != null) put("dob", dob)
                    put("gender", gender)
                    if (profilePicture != null) put("profile_picture", profilePicture)
                }

                // Send request
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonPayload.toString())
                    writer.flush()
                }

                // Read response
                val responseCode = connection.responseCode
                val reader = if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream))
                } else {
                    BufferedReader(InputStreamReader(connection.errorStream))
                }

                val response = reader.use { it.readText() }
                val jsonResponse = JSONObject(response)

                val success = jsonResponse.getBoolean("success")
                val message = jsonResponse.getString("message")

                withContext(Dispatchers.Main) {
                    callback(success, message)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(false, "Network error: ${e.message}")
                }
            }
        }
    }
}
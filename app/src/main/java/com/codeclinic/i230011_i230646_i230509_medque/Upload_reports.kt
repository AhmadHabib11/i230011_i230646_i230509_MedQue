package com.codeclinic.i230011_i230646_i230509_medque

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.codeclinic.i230011_i230646_i230509_medque.utils.FirebaseRealtimeHelper
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

class Upload_reports : AppCompatActivity() {
    
    private val PICK_FILE_REQUEST = 1
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""
    private var selectedFileSize: String = ""
    
    private lateinit var uploadArea: CardView
    private lateinit var fileItemCard: CardView
    private lateinit var fileNameText: TextView
    private lateinit var fileSizeText: TextView
    private lateinit var btnRemove: ImageView
    private lateinit var btnSubmit: TextView
    
    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_reports)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        uploadArea = findViewById(R.id.upload_area)
        fileItemCard = findViewById(R.id.file_item_card)
        fileNameText = findViewById(R.id.file_name)
        fileSizeText = findViewById(R.id.file_size)
        btnRemove = findViewById(R.id.btn_remove)
        btnSubmit = findViewById(R.id.btn_submit)
        
        // Initially hide the file item card
        fileItemCard.visibility = View.GONE
    }
    
    private fun setupClickListeners() {
        val backbtn = findViewById<ImageView>(R.id.btn_back)
        backbtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
        
        // Upload area click
        uploadArea.setOnClickListener {
            openFilePicker()
        }
        
        // Remove file button
        btnRemove.setOnClickListener {
            removeSelectedFile()
        }
        
        // Submit button
        btnSubmit.setOnClickListener {
            if (selectedFileUri != null) {
                uploadReport()
            } else {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        val mimeTypes = arrayOf(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        filePickerLauncher.launch(intent)
    }
    
    private fun handleSelectedFile(uri: Uri) {
        selectedFileUri = uri
        
        // Get file name and size
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                
                selectedFileName = it.getString(nameIndex)
                val fileSize = it.getLong(sizeIndex)
                selectedFileSize = formatFileSize(fileSize)
                
                // Update UI
                fileNameText.text = selectedFileName
                fileSizeText.text = selectedFileSize
                fileItemCard.visibility = View.VISIBLE
            }
        }
    }
    
    private fun removeSelectedFile() {
        selectedFileUri = null
        selectedFileName = ""
        selectedFileSize = ""
        fileItemCard.visibility = View.GONE
        Toast.makeText(this, "File removed", Toast.LENGTH_SHORT).show()
    }
    
    private fun uploadReport() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get user ID from SharedPreferences (assuming you store it during login)
        val sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)
        
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Read file content
            val inputStream: InputStream? = contentResolver.openInputStream(selectedFileUri!!)
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length: Int
            
            inputStream?.use { input ->
                while (input.read(buffer).also { length = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, length)
                }
            }
            
            val fileBytes = byteArrayOutputStream.toByteArray()
            val fileBase64 = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT)
            
            // First save to Firebase Realtime Database
            val firebaseHelper = FirebaseRealtimeHelper()
            firebaseHelper.saveReportBase64(
                this,
                selectedFileUri!!,
                userId,
                "Medical Report",
                selectedFileName
            ) { success: Boolean, reportId: String?, message: String, base64String: String? ->
                if (success && base64String != null) {
                    // Then upload to MySQL via PHP
                    uploadToMySQL(userId, selectedFileName, base64String)
                } else {
                    Toast.makeText(this, "Firebase save failed: $message", Toast.LENGTH_LONG).show()
                }
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun uploadToMySQL(userId: Int, fileName: String, fileBase64: String) {
        val url = "http://192.168.1.4/medque_app/upload_report.php"
        
        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        Toast.makeText(this, "Report uploaded successfully to both Firebase and MySQL!", Toast.LENGTH_LONG).show()
                        // Clear the file and go back
                        removeSelectedFile()
                        val intent = Intent(this, Home::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "MySQL upload failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                params["file_name"] = fileName
                params["file_data"] = fileBase64
                return params
            }
        }
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun formatFileSize(size: Long): String {
        return when {
            size >= 1024 * 1024 * 1024 -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
            size >= 1024 * 1024 -> String.format("%.2f MB", size / (1024.0 * 1024.0))
            size >= 1024 -> String.format("%.2f KB", size / 1024.0)
            else -> "$size B"
        }
    }
}

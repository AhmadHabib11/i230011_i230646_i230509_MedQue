package com.codeclinic.i230011_i230646_i230509_medque.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class to manage Firebase Realtime Database operations
 * Stores files as Base64 encoded strings directly in Firebase
 */
class FirebaseRealtimeHelper {
    
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val profileImagesRef: DatabaseReference = database.getReference("profile_images")
    private val reportsRef: DatabaseReference = database.getReference("medical_reports")
    
    /**
     * Data class for profile image with Base64 data
     */
    data class ProfileImageData(
        val userId: Int = 0,
        val base64Image: String = "",
        val uploadTimestamp: String = "",
        val fileSize: Long = 0,
        val isDoctor: Boolean = false,
        val mimeType: String = "image/jpeg"
    )
    
    /**
     * Data class for medical report with Base64 data
     */
    data class ReportData(
        val reportId: String = "",
        val patientId: Int = 0,
        val reportType: String = "",
        val reportTitle: String = "",
        val base64File: String = "",
        val uploadTimestamp: String = "",
        val fileSize: Long = 0,
        val fileName: String = "",
        val mimeType: String = "application/pdf"
    )
    
    /**
     * Save profile image as Base64 to Firebase and return Base64 string for MySQL
     * @param context Application context
     * @param imageUri URI of the image
     * @param userId User ID
     * @param isDoctor Whether it's a doctor profile
     * @param callback Returns success, message, and Base64 string
     */
    fun saveProfileImageBase64(
        context: Context,
        imageUri: Uri,
        userId: Int,
        isDoctor: Boolean,
        callback: (Boolean, String, String?) -> Unit
    ) {
        try {
            // Convert image to Base64
            val base64Image = imageUriToBase64(context, imageUri)
            
            if (base64Image == null) {
                callback(false, "Failed to convert image to Base64", null)
                return
            }
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val imageData = ProfileImageData(
                userId = userId,
                base64Image = base64Image,
                uploadTimestamp = timestamp,
                fileSize = base64Image.length.toLong(),
                isDoctor = isDoctor,
                mimeType = "image/jpeg"
            )
            
            val userType = if (isDoctor) "doctors" else "patients"
            profileImagesRef.child(userType).child(userId.toString())
                .setValue(imageData)
                .addOnSuccessListener {
                    callback(true, "Image saved successfully", base64Image)
                }
                .addOnFailureListener { exception ->
                    callback(false, "Failed to save image: ${exception.message}", null)
                }
        } catch (e: Exception) {
            callback(false, "Error processing image: ${e.message}", null)
        }
    }
    
    /**
     * Get profile image as Base64 from Firebase
     * @param userId User ID
     * @param isDoctor Whether it's a doctor profile
     * @param callback Returns ProfileImageData or null
     */
    fun getProfileImageBase64(
        userId: Int,
        isDoctor: Boolean,
        callback: (ProfileImageData?) -> Unit
    ) {
        val userType = if (isDoctor) "doctors" else "patients"
        profileImagesRef.child(userType).child(userId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imageData = snapshot.getValue(ProfileImageData::class.java)
                    callback(imageData)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }
    
    /**
     * Save medical report as Base64 to Firebase and return Base64 string for MySQL
     * @param context Application context
     * @param fileUri URI of the PDF file
     * @param patientId Patient ID
     * @param reportType Type of report
     * @param reportTitle Title of the report
     * @param callback Returns success, reportId, message, and Base64 string
     */
    fun saveReportBase64(
        context: Context,
        fileUri: Uri,
        patientId: Int,
        reportType: String,
        reportTitle: String,
        callback: (Boolean, String?, String, String?) -> Unit
    ) {
        try {
            // Convert file to Base64
            val base64File = fileUriToBase64(context, fileUri)
            
            if (base64File == null) {
                callback(false, null, "Failed to convert file to Base64", null)
                return
            }
            
            // Check file size (recommend max 2MB after Base64 encoding)
            val fileSizeMB = base64File.length / (1024.0 * 1024.0)
            if (fileSizeMB > 2.0) {
                callback(false, null, "File too large (${String.format("%.2f", fileSizeMB)} MB). Please use a file smaller than 2MB", null)
                return
            }
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val reportId = reportsRef.child(patientId.toString()).push().key
            
            if (reportId == null) {
                callback(false, null, "Failed to generate report ID", null)
                return
            }
            
            val fileName = getFileNameFromUri(context, fileUri)
            val reportData = ReportData(
                reportId = reportId,
                patientId = patientId,
                reportType = reportType,
                reportTitle = reportTitle,
                base64File = base64File,
                uploadTimestamp = timestamp,
                fileSize = base64File.length.toLong(),
                fileName = fileName,
                mimeType = "application/pdf"
            )
            
            reportsRef.child(patientId.toString()).child(reportId)
                .setValue(reportData)
                .addOnSuccessListener {
                    callback(true, reportId, "Report saved successfully", base64File)
                }
                .addOnFailureListener { exception ->
                    callback(false, null, "Failed to save report: ${exception.message}", null)
                }
        } catch (e: Exception) {
            callback(false, null, "Error processing file: ${e.message}", null)
        }
    }
    
    /**
     * Get all reports for a patient
     * @param patientId Patient ID
     * @param callback Returns list of ReportData
     */
    fun getPatientReports(
        patientId: Int,
        callback: (List<ReportData>) -> Unit
    ) {
        reportsRef.child(patientId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reports = mutableListOf<ReportData>()
                    for (childSnapshot in snapshot.children) {
                        val report = childSnapshot.getValue(ReportData::class.java)
                        if (report != null) {
                            reports.add(report)
                        }
                    }
                    // Sort by timestamp (newest first)
                    reports.sortByDescending { it.uploadTimestamp }
                    callback(reports)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
    
    /**
     * Get a specific report
     * @param patientId Patient ID
     * @param reportId Report ID
     * @param callback Returns ReportData or null
     */
    fun getReport(
        patientId: Int,
        reportId: String,
        callback: (ReportData?) -> Unit
    ) {
        reportsRef.child(patientId.toString()).child(reportId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val report = snapshot.getValue(ReportData::class.java)
                    callback(report)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }
    
    /**
     * Delete report from Firebase
     * @param patientId Patient ID
     * @param reportId Report ID
     * @param callback Success/failure callback
     */
    fun deleteReport(
        patientId: Int,
        reportId: String,
        callback: (Boolean, String) -> Unit
    ) {
        reportsRef.child(patientId.toString()).child(reportId)
            .removeValue()
            .addOnSuccessListener {
                callback(true, "Report deleted successfully")
            }
            .addOnFailureListener { exception ->
                callback(false, "Failed to delete report: ${exception.message}")
            }
    }
    
    /**
     * Listen for real-time updates to patient reports
     * @param patientId Patient ID
     * @param callback Called whenever reports change
     */
    fun listenToReportsRealtime(
        patientId: Int,
        callback: (List<ReportData>) -> Unit
    ): DatabaseReference {
        val reportsListener = reportsRef.child(patientId.toString())
        reportsListener.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reports = mutableListOf<ReportData>()
                for (childSnapshot in snapshot.children) {
                    val report = childSnapshot.getValue(ReportData::class.java)
                    if (report != null) {
                        reports.add(report)
                    }
                }
                reports.sortByDescending { it.uploadTimestamp }
                callback(reports)
            }
            
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
        return reportsListener
    }
    
    // Helper functions
    
    /**
     * Convert image URI to Base64 string (with compression)
     */
    private fun imageUriToBase64(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Compress image to reduce size
            val outputStream = ByteArrayOutputStream()
            
            // Scale down if too large
            val maxWidth = 1024
            val maxHeight = 1024
            val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                val ratio = Math.min(
                    maxWidth.toFloat() / bitmap.width,
                    maxHeight.toFloat() / bitmap.height
                )
                val width = (ratio * bitmap.width).toInt()
                val height = (ratio * bitmap.height).toInt()
                Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }
            
            // Compress to JPEG with 80% quality
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // Convert to Base64
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert file URI to Base64 string
     */
    private fun fileUriToBase64(context: Context, fileUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get filename from URI
     */
    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
    
    companion object {
        @Volatile
        private var INSTANCE: FirebaseRealtimeHelper? = null
        
        fun getInstance(): FirebaseRealtimeHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseRealtimeHelper().also { INSTANCE = it }
            }
        }
        
        /**
         * Convert Base64 string to Bitmap
         */
        fun base64ToBitmap(base64Str: String): Bitmap? {
            return try {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        
        /**
         * Convert Base64 string to ByteArray (for PDFs)
         */
        fun base64ToByteArray(base64Str: String): ByteArray? {
            return try {
                Base64.decode(base64Str, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

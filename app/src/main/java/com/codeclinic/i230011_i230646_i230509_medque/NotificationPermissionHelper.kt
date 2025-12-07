package com.codeclinic.i230011_i230646_i230509_medque

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object NotificationPermissionHelper {

    const val PERMISSION_REQUEST_CODE = 1001

    fun checkAndRequestPermission(activity: Activity): Boolean {
        // Only needed for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS

            return if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(permission),
                    PERMISSION_REQUEST_CODE
                )
                false
            } else {
                // Permission already granted
                true
            }
        }

        // For Android 12 and below, permission is granted by default
        return true
    }

    fun isPermissionGranted(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

// Add this to your login activity or home activity
/*
Usage in Activity:

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    // Request notification permission
    NotificationPermissionHelper.checkAndRequestPermission(this)
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == NotificationPermissionHelper.PERMISSION_REQUEST_CODE) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, register FCM token
            val userId = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
                .getInt("user_id", -1)
            if (userId != -1) {
                FCMTokenHelper.registerFCMToken(this, userId)
            }
        } else {
            // Permission denied
            Toast.makeText(this,
                "Notification permission denied. You won't receive appointment updates.",
                Toast.LENGTH_LONG).show()
        }
    }
}
*/
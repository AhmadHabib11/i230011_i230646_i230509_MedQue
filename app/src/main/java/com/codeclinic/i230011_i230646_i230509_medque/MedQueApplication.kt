package com.codeclinic.i230011_i230646_i230509_medque

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MedQueApplication : Application() {

    companion object {
        private const val TAG = "MedQueApp"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "🚀 Application starting...")

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "✅ Firebase initialized")

        // Initialize FCM and create notification channel
        FCMTokenHelper.initializeFCM(this)
        Log.d(TAG, "✅ FCM initialized")

        // Get FCM token immediately
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "🔥 FCM Token on startup: $token")

                // Save token locally for later use
                val sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("pending_fcm_token", token).apply()

                // If user is logged in, send token immediately
                val userId = sharedPreferences.getInt("user_id", -1)
                if (userId != -1) {
                    Log.d(TAG, "User logged in, registering token for user: $userId")
                    FCMTokenHelper.registerFCMToken(this, userId)
                }
            } else {
                Log.e(TAG, "❌ Failed to get FCM token", task.exception)
            }
        }

        Log.d(TAG, "✅ Application initialization complete")
    }
}
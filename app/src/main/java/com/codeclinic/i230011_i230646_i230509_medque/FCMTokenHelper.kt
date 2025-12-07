package com.codeclinic.i230011_i230646_i230509_medque

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

object FCMTokenHelper {

    private const val TAG = "FCMTokenHelper"
    private const val BASE_URL = "http://192.168.100.22/medque_app"

    /**
     * Initialize FCM and create notification channel
     */
    fun initializeFCM(context: Context) {
        // Create notification channel
        createNotificationChannel(context)
        Log.d(TAG, "FCM initialized and notification channel created")
    }

    /**
     * Create notification channel for Android O+
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = MyFirebaseMessagingService.CHANNEL_ID
            val channelName = "MedQue Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "MedQue appointment notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "✅ Notification channel created: $channelId")
        }
    }

    /**
     * Register FCM token and send to server
     */
    fun registerFCMToken(context: Context, userId: Int) {
        Log.d(TAG, "Registering FCM token for user: $userId")

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "❌ Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "🔥 FCM Token obtained: $token")

            // Send to server
            sendTokenToServer(context, userId, token)
        }
    }

    /**
     * Send token to PHP backend
     */
    private fun sendTokenToServer(context: Context, userId: Int, token: String) {
        val url = "$BASE_URL/update_fcm_token.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
            put("fcm_token", token)
        }

        Log.d(TAG, "Sending token to server for user $userId")

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonObject,
            { response ->
                Log.d(TAG, "✅ FCM token sent successfully: $response")

                // Clear any pending token
                val sharedPreferences = context.getSharedPreferences("MedQuePrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().remove("pending_fcm_token").apply()
            },
            { error ->
                Log.e(TAG, "❌ Error sending FCM token: ${error.message}")
                error.printStackTrace()

                // Save as pending to retry later
                val sharedPreferences = context.getSharedPreferences("MedQuePrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putString("pending_fcm_token", token).apply()
            }
        )

        Volley.newRequestQueue(context).add(request)
    }

    /**
     * Check for pending token and send it
     */
    fun checkAndSendPendingToken(context: Context, userId: Int) {
        Log.d(TAG, "Checking for pending FCM token for user: $userId")

        val sharedPreferences = context.getSharedPreferences("MedQuePrefs", Context.MODE_PRIVATE)
        val pendingToken = sharedPreferences.getString("pending_fcm_token", null)

        if (pendingToken != null) {
            Log.d(TAG, "📤 Sending pending FCM token")
            sendTokenToServer(context, userId, pendingToken)
        } else {
            // Get fresh token
            Log.d(TAG, "No pending token, fetching new one")
            registerFCMToken(context, userId)
        }
    }

    /**
     * Test notification - for debugging (works on all Android versions)
     */
    fun sendTestNotification(context: Context) {
        createNotificationChannel(context) // Ensure channel exists

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(context, MyFirebaseMessagingService.CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(context)
        }

        builder.setSmallIcon(R.drawable.logo)
            .setContentTitle("Test Notification")
            .setContentText("Local notifications are working! FCM setup is correct.")
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(android.app.Notification.CATEGORY_MESSAGE)
        }

        notificationManager.notify(999, builder.build())
        Log.d(TAG, "✅ Test notification sent")
    }
}
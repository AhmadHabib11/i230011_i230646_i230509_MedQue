package com.codeclinic.i230011_i230646_i230509_medque

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID = "medque_notifications"
        private const val CHANNEL_NAME = "MedQue Notifications"
    }

    override fun onCreate() {
        super.onCreate()
        // Create notification channel when service starts
        createNotificationChannel()
        Log.d(TAG, "FCM Service created and notification channel initialized")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔥 New FCM token generated: $token")

        // Save token locally
        val sharedPreferences = getSharedPreferences("MedQuePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("pending_fcm_token", token).apply()

        // Try to send to server if user is logged in
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
            sendTokenToServer(token, userId)
        } else {
            Log.w(TAG, "User not logged in, token will be sent on next login")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📩 Message received from: ${remoteMessage.from}")
        Log.d(TAG, "📩 Message data: ${remoteMessage.data}")
        Log.d(TAG, "📩 Notification: ${remoteMessage.notification}")

        // Ensure notification channel exists
        createNotificationChannel()

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification Title: ${notification.title}")
            Log.d(TAG, "Notification Body: ${notification.body}")

            sendNotification(
                title = notification.title ?: "MedQue",
                message = notification.body ?: "",
                data = remoteMessage.data
            )
        }

        // Handle data payload (when app is in foreground)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Processing data payload")
            handleDataMessage(remoteMessage.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "MedQue"
        val message = data["message"] ?: ""

        Log.d(TAG, "Handling data message - Title: $title, Message: $message")

        sendNotification(title, message, data)
    }

    private fun sendNotification(title: String, message: String, data: Map<String, String>) {
        Log.d(TAG, "🔔 Creating notification - Title: $title")

        // Create intent based on notification type
        val intent = when (data["type"]) {
            "appointment_booked", "appointment_cancelled", "appointment_rescheduled" -> {
                Intent(this, Notifications::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("notification_type", data["type"])
                    putExtra("appointment_id", data["appointment_id"])
                }
            }
            else -> {
                Intent(this, home::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            pendingIntentFlags
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVibrate(longArrayOf(0, 500, 200, 500))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "✅ Notification displayed with ID: $notificationId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "MedQue appointment notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "✅ Notification channel created: $CHANNEL_ID")
        }
    }

    private fun sendTokenToServer(token: String, userId: Int) {
        val url = "http://192.168.100.22/medque_app/update_fcm_token.php"
        val jsonObject = org.json.JSONObject().apply {
            put("user_id", userId)
            put("fcm_token", token)
        }

        val request = com.android.volley.toolbox.JsonObjectRequest(
            com.android.volley.Request.Method.POST,
            url,
            jsonObject,
            { response ->
                Log.d(TAG, "✅ Token sent to server successfully: $response")

                // Clear pending token
                val sharedPreferences = getSharedPreferences("MedQuePrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().remove("pending_fcm_token").apply()
            },
            { error ->
                Log.e(TAG, "❌ Error sending token to server: ${error.message}")
                error.printStackTrace()
            }
        )

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request)
    }
}
package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class NotificationRepository(context: Context) {

    private val BASE_URL = "http://192.168.18.37/medque_app"
    private val dao = NotificationDatabase.getDatabase(context).notificationDao()
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    // Load notifications (Local first, then sync with server)
    suspend fun loadNotifications(userId: Int): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            // 1. Load from local database first (instant display)
            val localNotifications = dao.getAllNotifications(userId)
            Log.d("NotificationRepo", "Loaded ${localNotifications.size} notifications from local DB")

            // Convert to Notification objects
            val notificationList = localNotifications.map { it.toNotification() }

            // 2. Sync with server in background
            try {
                syncWithServer(userId)
            } catch (e: Exception) {
                Log.e("NotificationRepo", "Server sync failed: ${e.message}")
                // Continue with local data even if sync fails
            }

            // 3. Return updated data from local DB
            val updatedNotifications = dao.getAllNotifications(userId).map { it.toNotification() }
            Result.success(updatedNotifications)

        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error loading notifications: ${e.message}")
            Result.failure(e)
        }
    }

    // Sync notifications from server to local database
    private suspend fun syncWithServer(userId: Int) = suspendCoroutine { continuation ->
        val url = "$BASE_URL/get_notifications.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("NotificationRepo", "Syncing with server for user: $userId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    if (response.getBoolean("success")) {
                        val dataArray = response.getJSONArray("data")
                        Log.d("NotificationRepo", "Server returned ${dataArray.length()} notifications")

                        val notifications = mutableListOf<NotificationEntity>()

                        for (i in 0 until dataArray.length()) {
                            val notifJson = dataArray.getJSONObject(i)

                            val isRead = when {
                                notifJson.has("is_read") && notifJson.get("is_read") is Boolean ->
                                    notifJson.getBoolean("is_read")
                                notifJson.has("is_read") ->
                                    notifJson.getInt("is_read") == 1
                                else -> false
                            }

                            val entity = NotificationEntity(
                                id = notifJson.getInt("id"),
                                userId = userId,
                                type = notifJson.getString("type"),
                                title = notifJson.getString("title"),
                                message = notifJson.getString("message"),
                                appointmentId = notifJson.optInt("appointment_id", 0).let { if (it == 0) null else it },
                                doctorName = notifJson.optString("doctor_name", null),
                                isRead = isRead,
                                createdAt = notifJson.getString("created_at"),
                                syncedAt = System.currentTimeMillis()
                            )

                            notifications.add(entity)
                        }

                        // Save to local database
                        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                            dao.insertAll(notifications)
                            Log.d("NotificationRepo", "Saved ${notifications.size} notifications to local DB")
                        }

                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(Exception("Server returned success=false"))
                    }
                } catch (e: Exception) {
                    Log.e("NotificationRepo", "Error parsing server response: ${e.message}")
                    continuation.resumeWithException(e)
                }
            },
            { error ->
                Log.e("NotificationRepo", "Network error: ${error.message}")
                continuation.resumeWithException(error)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    // Mark notification as read (update both local and server)
    suspend fun markAsRead(notificationId: Int, userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Update local database immediately
            dao.markAsRead(notificationId, userId)
            Log.d("NotificationRepo", "Marked notification $notificationId as read locally")

            // 2. Update server
            updateReadStatusOnServer(notificationId, userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking as read: ${e.message}")
            Result.failure(e)
        }
    }

    // Mark all notifications as read
    suspend fun markAllAsRead(userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Update local database immediately
            dao.markAllAsRead(userId)
            Log.d("NotificationRepo", "Marked all notifications as read locally")

            // 2. Update server
            markAllReadOnServer(userId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking all as read: ${e.message}")
            Result.failure(e)
        }
    }

    // Get unread count (from local database - instant)
    suspend fun getUnreadCount(userId: Int): Int = withContext(Dispatchers.IO) {
        dao.getUnreadCount(userId)
    }

    // Helper: Update read status on server
    private suspend fun updateReadStatusOnServer(notificationId: Int, userId: Int) = suspendCoroutine { continuation ->
        val url = "$BASE_URL/mark_notification_read.php"
        val jsonObject = JSONObject().apply {
            put("notification_id", notificationId)
            put("user_id", userId)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                if (response.getBoolean("success")) {
                    Log.d("NotificationRepo", "Server updated notification $notificationId")
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(Exception("Server update failed"))
                }
            },
            { error ->
                Log.e("NotificationRepo", "Server update error: ${error.message}")
                continuation.resumeWithException(error)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    // Helper: Mark all as read on server
    private suspend fun markAllReadOnServer(userId: Int) = suspendCoroutine { continuation ->
        val url = "$BASE_URL/mark_all_read.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                if (response.getBoolean("success")) {
                    Log.d("NotificationRepo", "Server marked all as read")
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(Exception("Server update failed"))
                }
            },
            { error ->
                Log.e("NotificationRepo", "Server update error: ${error.message}")
                continuation.resumeWithException(error)
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    // Extension function to convert Entity to Notification
    private fun NotificationEntity.toNotification(): Notification {
        return Notification(
            id = this.id,
            type = this.type,
            title = this.title,
            message = this.message,
            appointment_id = this.appointmentId,
            doctor_name = this.doctorName,
            is_read = this.isRead,
            created_at = this.createdAt
        )
    }
}
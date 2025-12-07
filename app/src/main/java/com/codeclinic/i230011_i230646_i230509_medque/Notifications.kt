package com.codeclinic.i230011_i230646_i230509_medque

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Notifications : AppCompatActivity() {

    private val BASE_URL = "http://192.168.100.22/medque_app"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var requestQueue: com.android.volley.RequestQueue
    private lateinit var newBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.notifications)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        requestQueue = Volley.newRequestQueue(this)

        newBadge = findViewById(R.id.newBadge)

        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        // Mark all as read buttons
        val markTodayRead = findViewById<TextView>(R.id.markTodayRead)
        markTodayRead?.setOnClickListener {
            markAllAsRead()
        }

        val markYesterdayRead = findViewById<TextView>(R.id.markYesterdayRead)
        markYesterdayRead?.setOnClickListener {
            markAllAsRead()
        }

        loadNotifications()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notificationsLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = sharedPreferences.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            Log.e("Notifications", "No user_id found in SharedPreferences")
            return
        }

        val url = "$BASE_URL/get_notifications.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        Log.d("Notifications", "=== LOADING NOTIFICATIONS ===")
        Log.d("Notifications", "URL: $url")
        Log.d("Notifications", "User ID: $userId")

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                Log.d("Notifications", "=== RESPONSE RECEIVED ===")
                Log.d("Notifications", "Full Response: $response")

                try {
                    val success = response.getBoolean("success")
                    Log.d("Notifications", "Success: $success")

                    if (success) {
                        val dataArray = response.getJSONArray("data")
                        Log.d("Notifications", "Number of notifications: ${dataArray.length()}")

                        val todayNotifications = mutableListOf<Notification>()
                        val yesterdayNotifications = mutableListOf<Notification>()
                        var unreadCount = 0

                        for (i in 0 until dataArray.length()) {
                            val notifJson = dataArray.getJSONObject(i)
                            Log.d("Notifications", "Notification $i: $notifJson")

                            // FIX: Handle is_read as integer (0 or 1)
                            val isRead = when {
                                notifJson.has("is_read") && notifJson.get("is_read") is Boolean ->
                                    notifJson.getBoolean("is_read")
                                notifJson.has("is_read") ->
                                    notifJson.getInt("is_read") == 1
                                else -> false
                            }

                            val notification = Notification(
                                id = notifJson.getInt("id"),
                                type = notifJson.getString("type"),
                                title = notifJson.getString("title"),
                                message = notifJson.getString("message"),
                                appointment_id = notifJson.optInt("appointment_id", 0).let { if (it == 0) null else it },
                                doctor_name = notifJson.optString("doctor_name", null),
                                is_read = isRead,
                                created_at = notifJson.getString("created_at")
                            )

                            if (!notification.is_read) unreadCount++

                            val isToday = notification.isToday()
                            Log.d("Notifications", "Notification ${notification.id}: isToday=$isToday, created_at=${notification.created_at}")

                            if (isToday) {
                                todayNotifications.add(notification)
                            } else {
                                yesterdayNotifications.add(notification)
                            }
                        }

                        Log.d("Notifications", "Today: ${todayNotifications.size}, Yesterday: ${yesterdayNotifications.size}, Unread: $unreadCount")

                        // Update badge
                        if (unreadCount > 0) {
                            newBadge.visibility = View.VISIBLE
                            newBadge.text = unreadCount.toString()
                        } else {
                            newBadge.visibility = View.GONE
                        }

                        displayNotifications(todayNotifications, yesterdayNotifications)
                    } else {
                        Log.e("Notifications", "Success = false in response")
                    }
                } catch (e: Exception) {
                    Log.e("Notifications", "Error parsing response: ${e.message}")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("Notifications", "=== NETWORK ERROR ===")
                Log.e("Notifications", "Error: ${error.message}")
                Log.e("Notifications", "Error details: ${error.networkResponse?.statusCode}")
                if (error.networkResponse?.data != null) {
                    Log.e("Notifications", "Response: ${String(error.networkResponse.data)}")
                }
                Toast.makeText(this, "Failed to load notifications: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun displayNotifications(todayList: List<Notification>, yesterdayList: List<Notification>) {
        Log.d("Notifications", "=== DISPLAYING NOTIFICATIONS ===")
        Log.d("Notifications", "Today count: ${todayList.size}, Yesterday count: ${yesterdayList.size}")

        try {
            // Find the ScrollView
            val scrollView = findViewById<android.widget.ScrollView>(R.id.main)
            if (scrollView == null) {
                Log.e("Notifications", "ScrollView 'main' not found!")
                return
            }

            val mainContent = scrollView.getChildAt(0) as? LinearLayout
            if (mainContent == null) {
                Log.e("Notifications", "Main LinearLayout not found!")
                return
            }

            Log.d("Notifications", "Main content children: ${mainContent.childCount}")

            // Find TODAY and YESTERDAY sections
            if (mainContent.childCount < 2) {
                Log.e("Notifications", "Not enough children in main content!")
                return
            }

            val todaySection = mainContent.getChildAt(0) as? LinearLayout
            val yesterdaySection = mainContent.getChildAt(1) as? LinearLayout

            if (todaySection == null || yesterdaySection == null) {
                Log.e("Notifications", "Sections not found! Today: $todaySection, Yesterday: $yesterdaySection")
                return
            }

            Log.d("Notifications", "Today section children before clear: ${todaySection.childCount}")
            Log.d("Notifications", "Yesterday section children before clear: ${yesterdaySection.childCount}")

            // Clear old notifications (keep headers - first child is the header)
            while (todaySection.childCount > 1) {
                todaySection.removeViewAt(1)
            }
            while (yesterdaySection.childCount > 1) {
                yesterdaySection.removeViewAt(1)
            }

            Log.d("Notifications", "Sections cleared. Today children: ${todaySection.childCount}, Yesterday children: ${yesterdaySection.childCount}")

            // Show/hide sections based on content
            if (todayList.isEmpty() && yesterdayList.isEmpty()) {
                Log.d("Notifications", "No notifications to display")
                todaySection.visibility = View.GONE
                yesterdaySection.visibility = View.GONE
            } else {
                // Add today's notifications
                if (todayList.isNotEmpty()) {
                    Log.d("Notifications", "Adding ${todayList.size} notifications to TODAY section")
                    todaySection.visibility = View.VISIBLE
                    todayList.forEachIndexed { index, notification ->
                        val card = createNotificationCard(notification)
                        todaySection.addView(card)
                        Log.d("Notifications", "Added notification $index to TODAY: ${notification.title}")
                    }
                } else {
                    Log.d("Notifications", "Hiding TODAY section (empty)")
                    todaySection.visibility = View.GONE
                }

                // Add yesterday's notifications
                if (yesterdayList.isNotEmpty()) {
                    Log.d("Notifications", "Adding ${yesterdayList.size} notifications to YESTERDAY section")
                    yesterdaySection.visibility = View.VISIBLE
                    yesterdayList.forEachIndexed { index, notification ->
                        val card = createNotificationCard(notification)
                        yesterdaySection.addView(card)
                        Log.d("Notifications", "Added notification $index to YESTERDAY: ${notification.title}")
                    }
                } else {
                    Log.d("Notifications", "Hiding YESTERDAY section (empty)")
                    yesterdaySection.visibility = View.GONE
                }
            }

            Log.d("Notifications", "Final - Today children: ${todaySection.childCount}, Yesterday children: ${yesterdaySection.childCount}")

        } catch (e: Exception) {
            Log.e("Notifications", "Error in displayNotifications: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createNotificationCard(notification: Notification): View {
        Log.d("Notifications", "Creating card for: ${notification.title}")

        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(R.layout.item_notification, null, false)

        // Add margin between cards
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 4 // 4dp separator
        card.layoutParams = params

        // Set background color
        if (notification.type == "appointment_booked" && !notification.is_read) {
            card.setBackgroundColor(resources.getColor(R.color.notification_bg_light_green, null))
        } else {
            card.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        // Set icon background
        val iconBg = card.findViewById<View>(R.id.iconBackground)
        iconBg.setBackgroundResource(notification.getBackgroundDrawable())

        // Set icon
        val icon = card.findViewById<ImageView>(R.id.notificationIcon)
        icon.setImageResource(notification.getIconResource())

        // Set title
        val title = card.findViewById<TextView>(R.id.notificationTitle)
        title.text = notification.title

        // Set time
        val time = card.findViewById<TextView>(R.id.notificationTime)
        time.text = notification.getTimeAgo()

        // Set message
        val message = card.findViewById<TextView>(R.id.notificationMessage)
        message.text = notification.message

        // Mark as read on click
        card.setOnClickListener {
            if (!notification.is_read) {
                markNotificationAsRead(notification.id)
            }
        }

        Log.d("Notifications", "Card created successfully")
        return card
    }

    private fun markNotificationAsRead(notificationId: Int) {
        val userId = sharedPreferences.getInt("user_id", -1)
        val url = "$BASE_URL/mark_notification_read.php"
        val jsonObject = JSONObject().apply {
            put("notification_id", notificationId)
            put("user_id", userId)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                if (response.getBoolean("success")) {
                    loadNotifications()
                }
            },
            { error ->
                Log.e("Notifications", "Error: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun markAllAsRead() {
        val userId = sharedPreferences.getInt("user_id", -1)
        val url = "$BASE_URL/mark_all_read.php"
        val jsonObject = JSONObject().apply {
            put("user_id", userId)
        }

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show()
                    loadNotifications()
                }
            },
            { error ->
                Log.e("Notifications", "Error: ${error.message}")
                Toast.makeText(this, "Failed to mark all as read", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }
}
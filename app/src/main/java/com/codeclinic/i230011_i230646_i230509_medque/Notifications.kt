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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Notifications : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var repository: NotificationRepository
    private lateinit var newBadge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.notifications)

        sharedPreferences = getSharedPreferences("MedQuePrefs", MODE_PRIVATE)
        repository = NotificationRepository(this)

        newBadge = findViewById(R.id.newBadge)

        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }

        // Mark all as read buttons
        findViewById<TextView>(R.id.markTodayRead)?.setOnClickListener {
            markAllAsRead()
        }

        findViewById<TextView>(R.id.markYesterdayRead)?.setOnClickListener {
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
            return
        }

        lifecycleScope.launch {
            try {
                // Load from repository (local first, then syncs with server)
                val result = repository.loadNotifications(userId)

                result.onSuccess { notifications ->
                    Log.d("Notifications", "Loaded ${notifications.size} notifications")

                    val todayNotifications = mutableListOf<Notification>()
                    val yesterdayNotifications = mutableListOf<Notification>()
                    var unreadCount = 0

                    for (notification in notifications) {
                        if (!notification.is_read) unreadCount++

                        if (notification.isToday()) {
                            todayNotifications.add(notification)
                        } else {
                            yesterdayNotifications.add(notification)
                        }
                    }

                    // Update badge
                    if (unreadCount > 0) {
                        newBadge.visibility = View.VISIBLE
                        newBadge.text = unreadCount.toString()
                    } else {
                        newBadge.visibility = View.GONE
                    }

                    displayNotifications(todayNotifications, yesterdayNotifications)
                }

                result.onFailure { error ->
                    Log.e("Notifications", "Error: ${error.message}")
                    Toast.makeText(this@Notifications, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("Notifications", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun displayNotifications(todayList: List<Notification>, yesterdayList: List<Notification>) {
        try {
            val scrollView = findViewById<android.widget.ScrollView>(R.id.main)
            val mainContent = scrollView.getChildAt(0) as LinearLayout

            val todaySection = mainContent.getChildAt(0) as LinearLayout
            val yesterdaySection = mainContent.getChildAt(1) as LinearLayout

            // Clear old notifications
            while (todaySection.childCount > 1) {
                todaySection.removeViewAt(1)
            }
            while (yesterdaySection.childCount > 1) {
                yesterdaySection.removeViewAt(1)
            }

            // Show/hide sections
            if (todayList.isEmpty() && yesterdayList.isEmpty()) {
                todaySection.visibility = View.GONE
                yesterdaySection.visibility = View.GONE
            } else {
                if (todayList.isNotEmpty()) {
                    todaySection.visibility = View.VISIBLE
                    for (notification in todayList) {
                        todaySection.addView(createNotificationCard(notification))
                    }
                } else {
                    todaySection.visibility = View.GONE
                }

                if (yesterdayList.isNotEmpty()) {
                    yesterdaySection.visibility = View.VISIBLE
                    for (notification in yesterdayList) {
                        yesterdaySection.addView(createNotificationCard(notification))
                    }
                } else {
                    yesterdaySection.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            Log.e("Notifications", "Display error: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createNotificationCard(notification: Notification): View {
        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(R.layout.item_notification, null, false)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 4
        card.layoutParams = params

        if (notification.type == "appointment_booked" && !notification.is_read) {
            card.setBackgroundColor(resources.getColor(R.color.notification_bg_light_green, null))
        } else {
            card.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        card.findViewById<View>(R.id.iconBackground).setBackgroundResource(notification.getBackgroundDrawable())
        card.findViewById<ImageView>(R.id.notificationIcon).setImageResource(notification.getIconResource())
        card.findViewById<TextView>(R.id.notificationTitle).text = notification.title
        card.findViewById<TextView>(R.id.notificationTime).text = notification.getTimeAgo()
        card.findViewById<TextView>(R.id.notificationMessage).text = notification.message

        card.setOnClickListener {
            if (!notification.is_read) {
                markNotificationAsRead(notification.id)
            }
        }

        return card
    }

    private fun markNotificationAsRead(notificationId: Int) {
        val userId = sharedPreferences.getInt("user_id", -1)

        lifecycleScope.launch {
            repository.markAsRead(notificationId, userId)
            loadNotifications()
        }
    }

    private fun markAllAsRead() {
        val userId = sharedPreferences.getInt("user_id", -1)

        lifecycleScope.launch {
            val result = repository.markAllAsRead(userId)
            result.onSuccess {
                Toast.makeText(this@Notifications, "All notifications marked as read", Toast.LENGTH_SHORT).show()
                loadNotifications()
            }
            result.onFailure {
                Toast.makeText(this@Notifications, "Failed to mark all as read", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package com.codeclinic.i230011_i230646_i230509_medque

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfile : AppCompatActivity() {

    private lateinit var genderLayout: RelativeLayout
    private lateinit var tvGender: TextView
    private lateinit var btnBack: ImageView
    private lateinit var dobLayout: RelativeLayout
    private lateinit var tvDateOfBirth: TextView
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.editprofile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_screen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        genderLayout = findViewById(R.id.genderLayout)
        tvGender = findViewById(R.id.tvGender)
        //btnBack = findViewById(R.id.btnBack)
        dobLayout = findViewById(R.id.dobLayout)
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
    }

    private fun setupListeners() {
        // Gender selection
        genderLayout.setOnClickListener {
            showGenderPopup(it)
        }

        // Date of birth selection
        dobLayout.setOnClickListener {
            showDatePicker()
        }

        // Back button
        //btnBack.setOnClickListener {
          //  finish()
        //}
    }

    private fun showGenderPopup(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Male")
        popup.menu.add(0, 2, 1, "Female")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    tvGender.text = "Male"
                    tvGender.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                    true
                }
                2 -> {
                    tvGender.text = "Female"
                    tvGender.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showDatePicker() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Update calendar with selected date
                calendar.set(year, month, dayOfMonth)

                // Format and display the date
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)

                tvDateOfBirth.text = formattedDate
                tvDateOfBirth.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            },
            currentYear,
            currentMonth,
            currentDay
        )

        // Optional: Set maximum date to today (can't select future dates)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Optional: Set minimum date (e.g., 100 years ago for realistic birth dates)
        val minCalendar = Calendar.getInstance()
        minCalendar.set(currentYear - 100, 0, 1)
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        datePickerDialog.show()
    }
}
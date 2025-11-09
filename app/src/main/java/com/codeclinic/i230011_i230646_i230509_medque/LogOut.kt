package com.codeclinic.i230011_i230646_i230509_medque

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LogOut : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.logout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.logout_screen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Cancel button click listener
        val cancelButton = findViewById<TextView>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            finish() // Go back to the previous screen (Profile)
        }
    }
}
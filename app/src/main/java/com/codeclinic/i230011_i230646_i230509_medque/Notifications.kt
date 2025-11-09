package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Notifications : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.notifications)

        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }


    }
}
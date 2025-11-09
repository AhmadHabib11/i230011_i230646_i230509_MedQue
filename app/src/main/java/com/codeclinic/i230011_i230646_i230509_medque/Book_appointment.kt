package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Book_appointment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_appointment)

        val back_btn = findViewById<ImageView>(R.id.btn_back)
        back_btn.setOnClickListener {
            val intent = Intent(this, Doctor_detail::class.java)
            startActivity(intent)
            finish()
        }

        val confirm_btn = findViewById<TextView>(R.id.btn_confirm)
        confirm_btn.setOnClickListener {
            val intent = Intent(this, Appointment_done::class.java)
            startActivity(intent)
            finish()
        }






        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
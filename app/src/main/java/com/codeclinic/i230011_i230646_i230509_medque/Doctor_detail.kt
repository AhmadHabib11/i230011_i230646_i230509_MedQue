package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Doctor_detail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_detail)

        val back = findViewById<ImageView>(R.id.btn_back)
        back.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
            finish()
        }

        val book = findViewById<TextView>(R.id.btn_book_appointment)
        book.setOnClickListener {
            val intent = Intent(this, Book_appointment::class.java)
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
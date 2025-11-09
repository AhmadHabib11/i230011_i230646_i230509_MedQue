package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SetUpProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.setupprofile)

        val savebtn = findViewById<Button>(R.id.btnSave)
        savebtn.setOnClickListener {
            val intent = Intent(this, ProfileSetUpSuccess::class.java)
            startActivity(intent)
            finish()
        }

        val backarr = findViewById<ImageView>(R.id.btnBack)
        backarr.setOnClickListener {

            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setupprofile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
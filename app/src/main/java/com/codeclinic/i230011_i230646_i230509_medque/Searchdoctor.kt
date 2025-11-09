package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Searchdoctor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_searchdoctor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val back_btn = findViewById<ImageView>(R.id.btnBack)

        back_btn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
        val home_btn = findViewById<ImageView>(R.id.homebt)

        home_btn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
        val gynbtn = findViewById<TextView>(R.id.btnGynecologist)

        gynbtn.setOnClickListener {
            val intent = Intent(this, Search_gyn::class.java)
            startActivity(intent)
            finish()
        }
        val cardbtn = findViewById<TextView>(R.id.btnCardiologist)

        cardbtn.setOnClickListener {
            val intent = Intent(this, Search_cardiologist::class.java)
            startActivity(intent)
            finish()
        }
        val navcalender = findViewById<ImageView>(R.id.calendarbt)

        navcalender.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
            finish()
        }
        val prof = findViewById<ImageView>(R.id.profilebt)
        prof.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }


        val sdoc1 = findViewById<RelativeLayout>(R.id.doctor1Card)
        sdoc1.setOnClickListener {
            val intent = Intent(this, Doctor_detail::class.java)
            startActivity(intent)
            finish()
        }




    }
}
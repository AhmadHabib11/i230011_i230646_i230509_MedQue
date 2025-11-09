package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Handle edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.act_home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val uploadReportBtn = findViewById<TextView>(R.id.upload_report_btn)

        uploadReportBtn.setOnClickListener {
            val intent = Intent(this, Upload_reports::class.java)
            startActivity(intent)
            finish()
        }

        val searchdoct = findViewById<ImageView>(R.id.search_doc)

        searchdoct.setOnClickListener {
            val intent = Intent(this, Searchdoctor::class.java)
            startActivity(intent)
            finish()
        }
        val navcalender = findViewById<ImageView>(R.id.calenderbtn)

        navcalender.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
            finish()
        }
        val bookbtn = findViewById<TextView>(R.id.btnAll)

        bookbtn.setOnClickListener {
            val intent = Intent(this, UpcomingAppointments::class.java)
            startActivity(intent)
            finish()
        }


        val notifbtn = findViewById<ImageView>(R.id.notification_icon)
        notifbtn.setOnClickListener {
            val intent = Intent(this, Notifications::class.java)
            startActivity(intent)
            finish()
        }

        val prof = findViewById<ImageView>(R.id.personalbtn)
        prof.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }



    }
}

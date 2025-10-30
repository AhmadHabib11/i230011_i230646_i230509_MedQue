package com.codeclinic.i230011_i230646_i230509_medque

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


import android.content.Intent
import android.os.Handler
import android.os.Looper

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.splashscreen)

                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, Onboarding::class.java)
                    startActivity(intent)
                    finish()
                }, 5000)

    }
}

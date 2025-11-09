package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)

        val createaccbtn = findViewById<Button>(R.id.createAccountButton)
        createaccbtn.setOnClickListener {
            val intent = Intent(this, SetUpProfile::class.java)
            startActivity(intent)
            finish()
        }

        val signinlink = findViewById<TextView>(R.id.signInLink)
        signinlink.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()

        }


    }

}
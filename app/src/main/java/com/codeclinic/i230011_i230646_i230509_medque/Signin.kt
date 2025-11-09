package com.codeclinic.i230011_i230646_i230509_medque

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Signin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signin)


        val signinbtn = findViewById<Button>(R.id.signInButton)
        signinbtn.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val signupbtn = findViewById<TextView>(R.id.signUpLink)
        signupbtn.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }


        val forgotpasswordbtn = findViewById<TextView>(R.id.forgotPasswordLink)
        forgotpasswordbtn.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
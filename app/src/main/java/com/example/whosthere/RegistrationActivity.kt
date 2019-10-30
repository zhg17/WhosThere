package com.example.whosthere

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.util.Log

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


class RegistrationActivity : AppCompatActivity() {

    private var emailTV: EditText? = null
    private var passwordTV: EditText? = null
    private var regBtn: Button? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()

        //Initializing views
        emailTV = findViewById(R.id.register_email)
        passwordTV = findViewById(R.id.register_pass)
        regBtn = findViewById(R.id.register_button)

        regBtn!!.setOnClickListener { registerNewUser() }
    }

    private fun registerNewUser() {

        val email: String
        val password: String
        email = emailTV!!.text.toString()
        password = passwordTV!!.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter password!", Toast.LENGTH_LONG).show()
            return
        }

        val x = mAuth!!.createUserWithEmailAndPassword(email, password)
        x.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()

                val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
            }
        }
    }

}

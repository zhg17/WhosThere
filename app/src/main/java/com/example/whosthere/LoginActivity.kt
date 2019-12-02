package com.example.whosthere

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.text.method.PasswordTransformationMethod

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference


class LoginActivity : AppCompatActivity() {

    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var loginBtn: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        val intent = intent
        uid = intent.getStringExtra("uid")


        //Initializing views
        userEmail = findViewById(R.id.login_email)
        userPassword = findViewById(R.id.login_pass)
        loginBtn = findViewById(R.id.login_button)

        userPassword?.setTransformationMethod(PasswordTransformationMethod.getInstance())

        loginBtn!!.setOnClickListener { loginUserAccount() }
    }

    private fun loginUserAccount() {

        val currEmail = findViewById<EditText>(R.id.login_email).text.toString()
        val currPass = findViewById<EditText>(R.id.login_pass).text.toString()

        if (TextUtils.isEmpty(currEmail)) {
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }

        if (TextUtils.isEmpty(currPass)) {
            Toast.makeText(applicationContext, "Please enter password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(currEmail, currPass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this@LoginActivity, MapActivity::class.java)
                    intent.putExtra("uid", uid)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Login failed! Please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

    }
}

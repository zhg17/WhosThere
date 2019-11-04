package com.example.whosthere

import android.content.Intent
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {

    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var loginBtn: Button? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        //Initializing views
        userEmail = findViewById(R.id.login_email)
        userPassword = findViewById(R.id.login_pass)
        loginBtn = findViewById(R.id.login_button)

        userPassword?.setTransformationMethod(PasswordTransformationMethod.getInstance())

        loginBtn!!.setOnClickListener { loginUserAccount() }
    }

    private fun loginUserAccount() {

        // Todo : Retrieve eamil and password, make sure it's not empty
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

        // Todo : Signin with given Email and Password
        // Retrieve UID for Current User if Login successful and store in intent, for the key UserID
        // Start Intent DashboardActivity if Registration Successful



        mAuth!!.signInWithEmailAndPassword(currEmail, currPass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this@LoginActivity, MapActivity::class.java)
                    intent.putExtra(UserID,mAuth!!.uid)

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
    companion object{
        val UserID="com.example.whosthere.UID"
    }

}

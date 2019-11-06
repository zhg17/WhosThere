package com.example.whosthere

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.util.Log
import android.location.Location

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class RegistrationActivity : AppCompatActivity() {

    private var emailTV: EditText? = null
    private var passwordTV: EditText? = null
    private var usernameTV:EditText?=null
    private var regBtn: Button? = null
    private var mAuth: FirebaseAuth? = null

    private var mDatabase:DatabaseReference?=null

    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()
        mDatabase=FirebaseDatabase.getInstance().getReference("Users")

        //Initializing views
        emailTV = findViewById(R.id.register_email)
        passwordTV = findViewById(R.id.register_pass)
        usernameTV=findViewById(R.id.username)
        regBtn = findViewById(R.id.register_button)

        passwordTV?.setTransformationMethod(PasswordTransformationMethod.getInstance())
        regBtn!!.setOnClickListener { registerNewUser() }


    }

    private fun registerNewUser() {

        val email: String
        val password: String
        email = emailTV!!.text.toString()
        password = passwordTV!!.text.toString()
        val username=usernameTV!!.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter password!", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(username)){
            Toast.makeText(applicationContext,"Please enter a user name!",Toast.LENGTH_LONG).show()
            return
        }

        val x = mAuth!!.createUserWithEmailAndPassword(email, password)
        x.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()

                saveUser(email,username)

                val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                intent.putExtra("uid", uid)
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUser(email: String,username:String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        val user = User(uid!!, email, arrayListOf(), 0.0,0.0,username)
        ref.child(uid!!).setValue(user).addOnCompleteListener{
            Toast.makeText(applicationContext, "User saved", Toast.LENGTH_LONG)
            Log.i("USER saved",uid)
        }
    }


}

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RegistrationActivity : AppCompatActivity() {

    private var emailTV: EditText? = null
    private var passwordTV: EditText? = null
    private var usernameTV:EditText?=null
    private var regBtn: Button? = null
    private var mAuth: FirebaseAuth? = null
    private var distanceTV: EditText? = null

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
        distanceTV = findViewById(R.id.register_distance)

        passwordTV?.setTransformationMethod(PasswordTransformationMethod.getInstance())
        regBtn!!.setOnClickListener { registerNewUser() }


    }

    private fun registerNewUser() {

        val email: String
        val password: String
        val distance: String
        email = emailTV!!.text.toString()
        password = passwordTV!!.text.toString()
        distance = distanceTV!!.text.toString()
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
        if (TextUtils.isEmpty(distance)){
            Toast.makeText(applicationContext,"Please enter a distance!",Toast.LENGTH_LONG).show()
            return
        }
        var hasusername=false
        mDatabase!!.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children){
                    val item = postSnapshot.getValue(User::class.java)
                    Log.i("CHECKOUT USER", item!!.username)
                    if (item!!.username == username) {
                        Toast.makeText(applicationContext, "User name has been used", Toast.LENGTH_LONG).show()
                        hasusername=true
                    }

                }
                if (!hasusername){
                    val x = mAuth!!.createUserWithEmailAndPassword(email, password)
                    x.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()

                            saveUser(email,username,distance)

                            val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                            intent.putExtra("uid", uid)
                            startActivity(intent)
                        } else {
                            Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }

    private fun saveUser(email: String,username:String, distance:String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        val user = User(uid!!, email, arrayListOf(), 0.0,0.0,username, distance)
        ref.child(uid!!).setValue(user).addOnCompleteListener{
            Toast.makeText(applicationContext, "User saved", Toast.LENGTH_LONG)
            Log.i("USER saved",uid)
        }
    }


}

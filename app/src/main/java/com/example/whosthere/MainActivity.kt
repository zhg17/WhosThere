package com.example.whosthere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button


class MainActivity : AppCompatActivity() {

    internal var mLoginBtn: Button? = null
    internal var mRegisterBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initializing views
        mLoginBtn = findViewById(R.id.loginBtn)
        mRegisterBtn = findViewById(R.id.registerBtn)

        mRegisterBtn!!.setOnClickListener {
            val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

    }
}

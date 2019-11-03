package com.example.whosthere

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class connection : AppCompatActivity(){

    internal lateinit var usernameView: TextView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        usernameView.findViewById<TextView>(R.id.username)

        val username=this.intent.getStringExtra("Userid")

        usernameView.setText(username)

    }
}
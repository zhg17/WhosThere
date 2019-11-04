package com.example.whosthere

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity(){

    private lateinit var usernameView: TextView
    private var databaseReference: DatabaseReference?=null
    private var database:FirebaseDatabase?=null


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        usernameView=findViewById(R.id.username)

        database= FirebaseDatabase.getInstance()

        databaseReference = database!!.reference!!.child("Users")
        Log.i("profile","profile page IN")

    }
    override fun onStart(){
        super.onStart()
        databaseReference!!.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (postSnapshor in dataSnapshot.children){
                    if (postSnapshor.key==intent.getStringExtra(MapActivity.UserID)){
                        for (p in postSnapshor.children){
                            val name=p.getValue<Users>(Users::class.java)
                            usernameView.setText(name.toString())
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError){}
        })
    }
    companion object{
        val UserID="com.example.whosthere.UID"
    }
}
package com.example.whosthere

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity(){

    private lateinit var usernameView: TextView
    private lateinit var emailView:TextView
    private var friendDBReference: DatabaseReference?=null
    private var userDBReference:DatabaseReference?=null
    private var database:FirebaseDatabase?=null
    private lateinit var addFriendbutton:Button

    private lateinit var friend: MutableList<Friend>
    internal lateinit var listViewFriends: ListView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        listViewFriends=findViewById(R.id.listViewFriends)
        usernameView=findViewById(R.id.usernameView)
        emailView=findViewById(R.id.emailView)
        database= FirebaseDatabase.getInstance()
        addFriendbutton=findViewById(R.id.addFriendbutton)

        friend=ArrayList()

        friendDBReference = database!!.reference!!.child("Friends")

       addFriendbutton.setOnClickListener{addFriendbutton()}
        Log.i("profile","profile page IN")

    }

    fun addFriendbutton(){
        val name = usernameView.text
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(applicationContext, "must enter a username...", Toast.LENGTH_LONG).show()
            return
        }

        //will search on db at table "User"
        //disply all user with same name (since name is not unique)
        //via some scrollable bar or sth
        //the user would choose who to add
    }
    override fun onStart(){
        super.onStart()
        //userDBReference!!.addValueEventListener(object:ValueEventListener{
         //   override fun onDataChange(dataSnapshot:DataSnapshot){

                //below line cause crash "DatabaseException : class android.locatioin does not define a no-argument constructor
                //see https://stackoverflow.com/questions/47166657/class-android-location-does-not-define-a-no-argument-constructor
                //we need to change how to store the location in the database becuase firebase doesn't support location class
                //********
          //      val item = dataSnapshot.getValue<User>(User::class.java)
                //********
          //      usernameView.text=item!!.username
            //    emailView.text=item!!.email


            //}
           // override fun onCancelled(databaseError:DatabaseError){}
        //})
        friendDBReference!!.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot){
                for (postSnapshor in dataSnapshot.children){
                    if (postSnapshor.key==intent.getStringExtra("uid")){
                        for (p in postSnapshor.children){
                            val name=p.getValue<Friend>(Friend::class.java)
                            friend.add(name!!)
                        }
                    }
                }
                val friendAdapter=UserList(this@ProfileActivity,friend)
                listViewFriends.adapter=friendAdapter

            }
            override fun onCancelled(databaseError: DatabaseError){}
        })
    }

}
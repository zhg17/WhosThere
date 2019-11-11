package com.example.whosthere

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity(){

    private lateinit var usernameView: TextView
    private lateinit var emailView:TextView
    private lateinit var locationView:TextView
    private var addFriendbutton:Button?=null
    private var searchFriendView:EditText?=null

    private var friendDBReference: DatabaseReference?=null
    private var userDBReference:DatabaseReference?=null
    private var database:FirebaseDatabase?=null

    private lateinit var friend: MutableList<String>
    internal lateinit var listViewFriends: ListView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        listViewFriends=findViewById(R.id.listViewFriends)
        usernameView=findViewById(R.id.usernameView)
        emailView=findViewById(R.id.emailView)
        searchFriendView=findViewById(R.id.search)
        locationView=findViewById(R.id.LocationView)
        database= FirebaseDatabase.getInstance()
        addFriendbutton=findViewById(R.id.addFriendbutton)

        friend=ArrayList()

        friendDBReference = database!!.reference!!.child("Friends")
        userDBReference=database!!.reference!!.child("Users").child(intent.getStringExtra("uid"))

        addFriendbutton!!.setOnClickListener{addFriendbutton()}
        Log.i("profile","profile page IN")

    }

    fun addFriendbutton(){
        var name = searchFriendView!!.text.toString()
        var myname=""
        if (TextUtils.isEmpty(name) ) {
            Toast.makeText(applicationContext, "must enter a username...", Toast.LENGTH_LONG).show()
            return
        }
        var hasuser=false
        database!!.reference!!.child("Users").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(dataSnapshot:DataSnapshot){
                for (postSnapshor in dataSnapshot.children){
                        val item = postSnapshor.getValue(User::class.java)
                        Log.i("CHECKOUT USER", item!!.username)
                        if (item!!.username == name) {
                            hasuser = true
                            Log.i("FOUND USER",hasuser.toString())
                        }
                        if(item!!.uid==intent.getStringExtra("uid")){
                            myname=item!!.username
                        }

                    }
                Log.i("FOUND",hasuser.toString())
                if (!hasuser){
                    Toast.makeText(applicationContext, "No such user", Toast.LENGTH_LONG).show()
                    return
                }
                if(myname==name){
                    Toast.makeText(applicationContext, "Cannot add yourself", Toast.LENGTH_LONG).show()
                    return
                }

                else {
                    //add to own friend list
                    userDBReference!!.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            friend.clear()
                            val item = dataSnapshot.getValue(User::class.java)
                            if (item!!.friends.contains(name)){
                                Toast.makeText(applicationContext,"Friend already added",Toast.LENGTH_LONG).show()
                                return
                            }
                            else {
                                for (i in item!!.friends) {
                                    friend.add(i)
                                }
                                friend.add(name)
                                val addlist = database!!.getReference("Users/" + intent.getStringExtra("uid")!! + "/friends")
                                addlist.setValue(friend)

                                //add me to target friend list
                                database!!.reference!!.child("Users")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            friend.clear()
                                            var theuid=""
                                            for (postSnapshor in dataSnapshot.children) {
                                                val item = postSnapshor.getValue(User::class.java)
                                                Log.i("CHECKOUT USER", item!!.username)
                                                if (item!!.username == name) {
                                                    theuid=item!!.uid
                                                    for ( i in item!!.friends){
                                                        friend.add(i)
                                                    }
                                                }
                                            }
                                            friend.add(myname)
                                            database!!.getReference("Users/" + theuid + "/friends").setValue((friend))
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {}
                                    })
                                Toast.makeText(applicationContext,"Friend is successfully added",Toast.LENGTH_LONG).show()
                                return
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
        //will search on db at table "User"
        //disply all user with same name (since name is not unique)
        //via some scrollable bar or sth
        //the user would choose who to add
    }
    override fun onStart(){
        super.onStart()
        userDBReference!!.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(dataSnapshot:DataSnapshot){
                val item = dataSnapshot.getValue<User>(User::class.java)
                usernameView.text="UserName: "+item!!.username
                emailView.text="UserEmail: "+item!!.email
                locationView.text="Location: "+item!!.lat.toString()+", "+item!!.long.toString()

            }
            override fun onCancelled(databaseError:DatabaseError){}
        })

    }

}
package com.example.whosthere

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.profile_dialog.view.*

class ProfileActivity : AppCompatActivity(){

    private lateinit var usernameView: TextView
    private lateinit var emailView:TextView
    private lateinit var locationView:TextView
    private var addFriendbutton:Button?=null
    private var searchFriendView:EditText?=null
    private var updateprofile:Button?=null
    private var current_username:String?=null
    private var userDBReference:DatabaseReference?=null
    private var database:FirebaseDatabase?=null
    private var removeFriendBtn:Button?=null

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
        updateprofile=findViewById(R.id.update)
        removeFriendBtn=findViewById(R.id.removeFriendbutton)

        friend=ArrayList()

        userDBReference=database!!.reference!!.child("Users").child(intent.getStringExtra("uid"))

        addFriendbutton!!.setOnClickListener{addFriendbutton()}
        updateprofile!!.setOnClickListener{updateprofile()}
        removeFriendBtn!!.setOnClickListener{removeFriend()}

        Log.i("profile","profile page IN")

    }
    private fun removeFriend(){
        var name = searchFriendView!!.text.toString()
        if (TextUtils.isEmpty(name) ) {
            Toast.makeText(applicationContext, "must enter a username...", Toast.LENGTH_LONG).show()
            return
        }
        userDBReference!!.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val item = dataSnapshot.getValue(User::class.java)
                if(!item!!.friends.contains(name)){
                    Toast.makeText(applicationContext, "You are not friend with this person", Toast.LENGTH_LONG).show()
                }
                else{
                    var list= arrayListOf<String>()
                    for (i in item!!.friends){
                        if(i!=name){
                            list.add(i)
                        }
                    }
                    val update=User(item!!.uid,item!!.email,list,item!!.lat,item!!.long,item!!.username)
                    userDBReference!!.setValue(update)

                    database!!.reference!!.child("Users").addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(datasnapshot2: DataSnapshot) {
                            for(postSnapshot2 in datasnapshot2.children){
                                val item2=postSnapshot2.getValue(User::class.java)
                                if (item2!!.username==name){
                                    var list2= arrayListOf<String>()
                                    for (i in item2!!.friends){
                                        if (i!=current_username){
                                            list2.add(i)
                                        }
                                    }
                                    val new_update=User(item2!!.uid,item2!!.email,list2,item2!!.lat,item2!!.long,item2!!.username)
                                    database!!.reference!!.child("Users").child(item2!!.uid).setValue(new_update)
                                    Toast.makeText(applicationContext, "Friend remove succeed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        override fun onCancelled(p0: DatabaseError) {}
                    })

                }

            }
            override fun onCancelled(p0: DatabaseError) {}
        })
        // add notification to notify the other user, friend relationship broke
    }
    private fun updateprofile(){
        val mDialogView=LayoutInflater.from(this).inflate(R.layout.profile_dialog,null)
        val mBuilder=AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Update Username")
        val mAlertDialog=mBuilder.show()
        mDialogView.confirm_update.setOnClickListener{
            mAlertDialog.dismiss()
            val name=mDialogView.name_update.text.toString()
            //// change the db
            Log.i("UPDATE_PROFIEL",name)
            database!!.reference!!.child("Users").addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(dataSnapshot:DataSnapshot){
                    for (postSnapshot in dataSnapshot.children){
                        val item = postSnapshot.getValue(User::class.java)
                        Log.i("UPDATE PROFILE FRIEND",item!!.friends.contains(current_username).toString())
                        Log.i("UPDATE PROFILE FRIEND",item!!.friends.toString())
                        if(item!!.friends.contains(current_username)){
                            var friends_list= arrayListOf<String>()
                            for (i in item!!.friends){
                                if (i!=current_username){
                                    friends_list.add(i)
                                }
                            }
                            friends_list.add(name)
                            val new_update=User(item!!.uid,item!!.email,friends_list,item!!.lat,item!!.long,item!!.username)
                            database!!.reference!!.child("Users").child(item!!.uid).setValue(new_update)
                        }

                    }
                }
                override fun onCancelled(p0:DatabaseError){}
            })
            userDBReference!!.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(dataSnapshot:DataSnapshot){
                    val item = dataSnapshot.getValue(User::class.java)
                    val update=User(item!!.uid,item!!.email,item!!.friends,item!!.lat,item!!.long,name)
                    userDBReference!!.setValue(update)
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })

        }
        Toast.makeText(applicationContext, "Update user name succeed", Toast.LENGTH_LONG).show()
        mDialogView.cancel_update.setOnClickListener{
            mAlertDialog.dismiss()
        }

    }
    private fun addFriendbutton(){
        var name = searchFriendView!!.text.toString()
        if (TextUtils.isEmpty(name) ) {
            Toast.makeText(applicationContext, "must enter a username...", Toast.LENGTH_LONG).show()
            return
        }
        if(current_username==name){
            Toast.makeText(applicationContext, "Cannot add yourself", Toast.LENGTH_LONG).show()
            return
        }
        var hasuser=false
        database!!.reference!!.child("Users").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(dataSnapshot:DataSnapshot){
                for (postSnapshot in dataSnapshot.children){
                    val item = postSnapshot.getValue(User::class.java)
                    Log.i("CHECKOUT USER", item!!.username)
                    if (item!!.username == name) {
                        hasuser = true
                        Log.i("FOUND USER",hasuser.toString())
                    }
                }
                Log.i("FOUND",hasuser.toString())
                if (!hasuser){
                    Toast.makeText(applicationContext, "No such user", Toast.LENGTH_LONG).show()
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
                                            friend.add(current_username.toString())
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
        //notify the other user they are now friend
    }
    override fun onStart(){
        super.onStart()
        userDBReference!!.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(dataSnapshot:DataSnapshot){
                val item = dataSnapshot.getValue<User>(User::class.java)
                current_username=item!!.username
                usernameView.text="UserName: "+item!!.username
                emailView.text="UserEmail: "+item!!.email
                locationView.text="Location: "+item!!.lat.toString()+", "+item!!.long.toString()

            }
            override fun onCancelled(databaseError:DatabaseError){}
        })

    }

}
package com.example.whosthere

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.text.TextUtils
import android.text.util.Linkify
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.profile_dialog.view.*
import kotlinx.android.synthetic.main.profile_dialog.view.confirm_update
import kotlinx.android.synthetic.main.profile_dialog_addfd.view.*

class ProfileActivity : AppCompatActivity(){

    private lateinit var usernameView: TextView
    private lateinit var emailView:TextView
    private lateinit var locationView:TextView
    private var editFriendbutton:Button?=null
    private var updateprofile:Button?=null
    private var current_username:String?=null
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
        locationView=findViewById(R.id.LocationView)
        database= FirebaseDatabase.getInstance()
        editFriendbutton=findViewById(R.id.editFriendbutton)
        updateprofile=findViewById(R.id.update)
        friend=ArrayList()

        userDBReference=database!!.reference!!.child("Users").child(intent.getStringExtra("uid"))

        editFriendbutton!!.setOnClickListener{editFriend()}
        updateprofile!!.setOnClickListener{updateprofile()}

        Log.i("profile","profile page IN")

    }
    private fun editFriend(){
        val mDialogView=LayoutInflater.from(this).inflate(R.layout.profile_dialog_addfd,null)
        val mBuilder=AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Update Friend List")
        val mAlertDialog=mBuilder.show()
        // dialog for update friends name

        mDialogView.confirm_button.setOnClickListener {
            mAlertDialog.dismiss()
            val name=mDialogView.search.text.toString()
            if (TextUtils.isEmpty(name) ) {
                Toast.makeText(applicationContext, "must enter a username...", Toast.LENGTH_LONG).show()
            }
            // check if the input is same as the current user
            if(current_username==name){
                Toast.makeText(applicationContext, "Cannot add yourself", Toast.LENGTH_LONG).show()
            }
            else {
                var hasuser = false
                // start add friend process
                database!!.reference!!.child("Users")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            //check is the user actually exist
                            for (postSnapshot in dataSnapshot.children) {
                                val item = postSnapshot.getValue(User::class.java)
                                Log.i("CHECKOUT USER", item!!.username)
                                if (item!!.username == name) {
                                    hasuser = true
                                    Log.i("FOUND USER", hasuser.toString())
                                }
                            }
                            Log.i("FOUND", hasuser.toString())
                            if (!hasuser) { // no such user in the database, abort
                                Toast.makeText(
                                    applicationContext,
                                    "No such user",
                                    Toast.LENGTH_LONG
                                ).show()
                                return
                            } else { // has this user in the db
                                //add to own friend list
                                userDBReference!!.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        friend.clear()
                                        val item = dataSnapshot.getValue(User::class.java)
                                        // if friends is already inside the list, abort
                                        if (item!!.friends.contains(name)) {
                                            Toast.makeText(
                                                applicationContext,
                                                "Friend already added",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        } else {  // the targe is not a friend with the current user yet
                                            for (i in item!!.friends) {
                                                friend.add(i)
                                            }
                                            friend.add(name)
                                            // update the new friend list
                                            val addlist = database!!.getReference(
                                                "Users/" + intent.getStringExtra("uid")!! + "/friends"
                                            )
                                            addlist.setValue(friend)

                                            //add current user  to target friend list
                                            database!!.reference!!.child("Users")
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        friend.clear()
                                                        var theuid = ""
                                                        // find the target friend
                                                        for (postSnapshor in dataSnapshot.children) {
                                                            val item =
                                                                postSnapshor.getValue(User::class.java)
                                                            Log.i("CHECKOUT USER", item!!.username)
                                                            if (item!!.username == name) {
                                                                // get the user id and the friend list
                                                                theuid = item!!.uid
                                                                for (i in item!!.friends) {
                                                                    friend.add(i)
                                                                }
                                                            }
                                                        }
                                                        //update the friend list for the target user
                                                        friend.add(current_username.toString())
                                                        database!!.getReference("Users/" + theuid + "/friends")
                                                            .setValue((friend))
                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {}
                                                })
                                            Toast.makeText(
                                                applicationContext,
                                                "Friend is successfully added",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return
                                        }
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {}
                                })
                            }
                        }
                        override fun onCancelled(p0: DatabaseError) {}
                    })
            }
        }
        mDialogView.remove_button.setOnClickListener{
            mAlertDialog.dismiss()
            val name=mDialogView.search.text.toString()
            if (TextUtils.isEmpty(name) ) {
                Toast.makeText(applicationContext, "must enter a username...", Toast.LENGTH_LONG).show()
            }
            // check if the input is same as the current user's name
            if (name==current_username) {
                Toast.makeText(applicationContext, "cannot remove yourself", Toast.LENGTH_LONG).show()
            }
            else {
                //start the remove process
                userDBReference!!.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val item = dataSnapshot.getValue(User::class.java)
                        //check if the user is friend with this person
                        if (!item!!.friends.contains(name)) {
                            Toast.makeText(
                                applicationContext,
                                "You are not friend with this person",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {  // the input is friend with the current user
                            var list = arrayListOf<String>()
                            // catch the friend for update without the target user
                            for (i in item!!.friends) {
                                if (i != name) {
                                    list.add(i)
                                }
                            }
                            // update the current user's value with new friend list
                            val update = User(
                                item!!.uid,
                                item!!.email,
                                list,
                                item!!.lat,
                                item!!.long,
                                item!!.username
                            )
                            userDBReference!!.setValue(update)
                            // remove the current user for the targeted user's friend list
                            database!!.reference!!.child("Users")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(datasnapshot2: DataSnapshot) {
                                        //find the target user
                                        for (postSnapshot2 in datasnapshot2.children) {
                                            val item2 = postSnapshot2.getValue(User::class.java)
                                            if (item2!!.username == name) {
                                                var list2 = arrayListOf<String>()
                                                // remove the current user from the current user's friend list
                                                for (i in item2!!.friends) {
                                                    if (i != current_username) {
                                                        list2.add(i)
                                                    }
                                                }
                                                //update the target user's friend list
                                                val new_update = User(
                                                    item2!!.uid,
                                                    item2!!.email,
                                                    list2,
                                                    item2!!.lat,
                                                    item2!!.long,
                                                    item2!!.username
                                                )
                                                database!!.reference!!.child("Users")
                                                    .child(item2!!.uid).setValue(new_update)
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Friend remove succeed",
                                                    Toast.LENGTH_LONG
                                                ).show()
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
        }
        mDialogView.cancel_button.setOnClickListener{
            mAlertDialog.dismiss()
        }
    }
    private fun updateprofile(){
        val mDialogView=LayoutInflater.from(this).inflate(R.layout.profile_dialog,null)
        val mBuilder=AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Update Username")
        val mAlertDialog=mBuilder.show()
        // dialog for input new user name
        mDialogView.confirm_update.setOnClickListener{
            mAlertDialog.dismiss()
            val name=mDialogView.name_update.text.toString()
            //check if the input is blank
            if (TextUtils.isEmpty(name) ) {
                Toast.makeText(applicationContext, "must enter a username...", Toast.LENGTH_LONG).show()
            }
            else {
                //// change the db
                Log.i("UPDATE_PROFIEL", name)
                database!!.reference!!.child("Users")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var hasthisname = false
                            //check if the new name has already been used
                            for (p in dataSnapshot.children) {
                                val i = p.getValue(User::class.java)
                                if (name == i!!.username) {
                                    hasthisname = true
                                }
                            }
                            if (hasthisname) {
                                Toast.makeText(
                                    applicationContext,
                                    "alread has this username",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else { //the new name is valid
                                //change the name for friends list
                                //for all user that is friend with the current user, change the data
                                for (postSnapshot in dataSnapshot.children) {
                                    val item = postSnapshot.getValue(User::class.java)
                                    if (item!!.friends.contains(current_username)) {
                                        var friends_list = arrayListOf<String>()
                                        //create a new friend list for update
                                        for (i in item!!.friends) {
                                            if (i != current_username) {
                                                friends_list.add(i)
                                            }
                                        }
                                        friends_list.add(name)
                                        val new_update = User(
                                            item!!.uid,
                                            item!!.email,
                                            friends_list,
                                            item!!.lat,
                                            item!!.long,
                                            item!!.username
                                        )
                                        //update the value of the user
                                        database!!.reference!!.child("Users").child(item!!.uid)
                                            .setValue(new_update)
                                    }

                                }
                                //change current user's username
                                userDBReference!!.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val item = dataSnapshot.getValue(User::class.java)
                                        val update = User(
                                            item!!.uid,
                                            item!!.email,
                                            item!!.friends,
                                            item!!.lat,
                                            item!!.long,
                                            name
                                        )
                                        userDBReference!!.setValue(update)
                                        Toast.makeText(applicationContext, "Update user name succeed", Toast.LENGTH_LONG).show()
                                    }

                                    override fun onCancelled(p0: DatabaseError) {
                                    }
                                })
                            }
                        }
                        override fun onCancelled(p0: DatabaseError) {}
                    })
            }
        }
        // close the dialog
        mDialogView.cancel_update.setOnClickListener{
            mAlertDialog.dismiss()
        }

    }
    override fun onStart(){
        super.onStart()
        // display the current user's info in the view
        userDBReference!!.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(dataSnapshot:DataSnapshot){
                val item = dataSnapshot.getValue<User>(User::class.java)
                current_username=item!!.username
                usernameView.text="UserName: "+item!!.username
                emailView.text="UserEmail: "+item!!.email
                locationView.text="Location: "+item!!.lat.toString()+", "+item!!.long.toString()
                val currentuser_friendlist=item!!.friends

                if (!currentuser_friendlist.isEmpty()){
                    var friendlist= arrayListOf<User>()
                    database!!.reference!!.child("Users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {
                                for (p00 in p0.children){
                                    val u = p00.getValue<User>(User::class.java)
                                    if (currentuser_friendlist.contains(u!!.username)){
                                        friendlist.add(u)
                                    }
                                }
                            }
                            override fun onCancelled(p0:DatabaseError){}
                        })
                    val friendlistAdapter=UserList(this@ProfileActivity,friendlist)
                    listViewFriends.adapter=friendlistAdapter
                }
            }
            override fun onCancelled(databaseError:DatabaseError){}
        })

    }

}
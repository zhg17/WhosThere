package com.example.whosthere

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log
import android.os.Looper
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import android.content.pm.PackageManager
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import android.os.Handler
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BackgroundLocationService : Service() {

    private val updateInterval = 1000 * 30
    private val fastestInterval = 1000 * 15
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var uid: String? = null
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var userReference: DatabaseReference
    private var friendsList = mutableMapOf<String, Boolean>()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.i(TAG, "Entered onCreate")
        super.onCreate()

        uid = currentUser!!.uid
        userReference = FirebaseDatabase.getInstance().getReference("Users")
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Creating notification channel
        val CHANNEL_ID = "Channel_0"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("")
            .setContentText("").build()

        startForeground(1, notification)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // getting friends only for the Current User
                val currFriends = dataSnapshot.child("$uid/friends").children

                //iterating through all the friends
                for (postSnapshot in currFriends) {
                    //getting friend
                    //val friend = postSnapshot.getValue<String>(User::class.java)!!.username
                    val friend = postSnapshot.value.toString()
                    //adding friend to the list
                    friendsList[friend] = false
                }
                Log.i(TAG, "Added friends complete")
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: called.")
        getLocation()
        // Stays active even when app is closed
        return START_STICKY
    }

    private fun getLocation() {
        // Creating location request
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = updateInterval.toLong()
        mLocationRequest.fastestInterval = fastestInterval.toLong()

        // Checking permissions
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission not granted, getLocation() stopping")
            stopSelf()
            return
        }
        Log.i(TAG, "Permission granted, getLocation() starting")
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    Log.i(TAG, "Location found")
                    val location = locationResult!!.lastLocation
                    if (location != null) {
                        updateLocation(location)
                        checkNearby()
                    }
                }
            }, Looper.myLooper() // Loops forever or until service is stopped
        )
    }

    private fun updateLocation(location: Location) {
        val lat=FirebaseDatabase.getInstance().getReference("Users/" + uid!! + "/lat")
        lat.setValue(location.latitude)
        val long=FirebaseDatabase.getInstance().getReference("Users/" + uid!! + "/long")
        long.setValue(location.longitude)
        Log.i(TAG, "Location updated to: ${location.latitude}, ${location.longitude}")
    }

    private fun checkNearby() {
        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (friend in friendsList) {
                    val allUsers = dataSnapshot.children
                    for (user in allUsers) {
                        if (friend.key == user.child("username").value.toString()) {
                            Log.i(TAG, "Found friend with name: $friend")
                            val currentLoc = Location("")
                            currentLoc.latitude = dataSnapshot.child("$uid/lat").value.toString().toDouble()
                            currentLoc.longitude = dataSnapshot.child("$uid/long").value.toString().toDouble()
                            val distanceSetting = dataSnapshot.child("$uid/distance").value.toString().toDouble()
                            Log.i(TAG, "Distance setting is $distanceSetting")
                            val friendLoc = Location("")
                            friendLoc.latitude = user.child("lat").value.toString().toDouble()
                            friendLoc.longitude = user.child("long").value.toString().toDouble()
                            val distanceInMeters = currentLoc.distanceTo(friendLoc)
                            //Log.i(TAG, "Distance in meters to friend: $distanceInMeters")
                            Log.i(TAG, "sent notification to ${friend.key}: ${friend.value}")
                            if ((distanceInMeters <= (distanceSetting*1609.34)) && !friend.value) {
                                friendsList[friend.key] = true
                                Log.i(TAG, "sending notification for ${friend.key}")
                                val notificationIntent = Intent(this@BackgroundLocationService, NotificationService::class.java)
                                notificationIntent.putExtra("friendName", friend.key)
                                startService(notificationIntent)
                                Handler().postDelayed({
                                    Log.i(TAG, "1 hour passed, resetting notification sent boolean for ${friend.key}")
                                    friendsList[friend.key] = false
                                }, 3600000)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    companion object {
        const val TAG = "BackgroundLocationService"
    }
}

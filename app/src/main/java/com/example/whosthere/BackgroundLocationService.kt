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
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BackgroundLocationService : Service() {

    private val updateInterval = 1000 * 30
    private val fastestInterval = 1000 * 15
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var uid: String? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.i(TAG, "Entered onCreate")
        super.onCreate()

        val currentUser = FirebaseAuth.getInstance().currentUser
        uid = currentUser!!.uid
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
                    }
                }
            }, Looper.myLooper() // Loops forever or until service is stopped
        )
    }

    private fun updateLocation(location: Location) {
        //val ref = FirebaseDatabase.getInstance().getReference("Users/" + uid!! + "/location")
        //ref.setValue(location)
        val lat=FirebaseDatabase.getInstance().getReference("Users/" + uid!! + "/lat")
        lat.setValue(location.latitude)
        val long=FirebaseDatabase.getInstance().getReference("Users/" + uid!! + "/long")
        long.setValue(location.longitude)
        Log.i(TAG, "Location updated to: ${location.latitude}, ${location.longitude}")
    }

    companion object {
        val TAG = "BackgroundLocationService"
    }
}

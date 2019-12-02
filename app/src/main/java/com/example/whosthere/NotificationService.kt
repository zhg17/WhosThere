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
import com.google.firebase.database.*
import android.graphics.Color
import android.app.PendingIntent
import android.app.Notification
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class NotificationService : Service() {

    companion object {
        private const val MY_NOTIFICATION_ID = 1
        private const val KEY_COUNT = "key_count"
        private lateinit var mNotificationManager: NotificationManager
        private lateinit var mChannelID: String
        // Notification Text Elements

        private const val tickerText = "This is a notification"
        private const val contentTitle = "Notification"
        private const val contentText = "You have a friend nearby!"
        private val mVibratePattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        private val TAG = "NotificationService"
        private var friendName: String? = null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "entered onStartCommand")
        friendName = intent.getStringExtra("friendName") as String
        Log.i(TAG, "Friend name is: $friendName")
        createNotificationChannel()

        if (intent.action == "com.example.whosthere.NotificationService") {
        } else {
            Log.d(TAG, "Received intent with action=" + intent.action + "; now what?")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        Log.i(TAG, "entered onCreate in NotificationService")
        super.onCreate()

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var mNotificationCount: Int = 0

    private fun createNotificationChannel() {
        mChannelID = "$packageName.channel_01"
        val name = getString(R.string.channel_name)
        val description = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(mChannelID, name, importance)
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = mVibratePattern
        mNotificationManager.createNotificationChannel(mChannel)
        sendNotification()
    }

    private fun sendNotification(){
        val mNotificationIntent = Intent(this@NotificationService, MapActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val mContentIntent = PendingIntent.getActivity(applicationContext, 0, mNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = Notification.Builder(
            applicationContext, mChannelID
        )
            .setTicker(tickerText)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setAutoCancel(true)
            .setContentTitle(contentTitle)
            .setContentText("Your friend $friendName is nearby!")
            .setContentIntent(mContentIntent)

        // Pass the Notification to the NotificationManager:
        mNotificationManager.notify(
            MY_NOTIFICATION_ID,
            notificationBuilder.build()
        )
        stopSelf()
    }

}

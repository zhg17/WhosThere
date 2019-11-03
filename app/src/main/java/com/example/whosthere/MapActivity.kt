package com.example.whosthere

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.location.LocationManager
import android.location.LocationListener
import android.location.Location
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.RuntimeException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.widget.Button


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mLocationManager: LocationManager? = null
    private var mLatitude: TextView? = null
    private var mLongitude: TextView? = null
    private var currLatitude: Double? = null
    private var currLongitude: Double? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    protected var mLastLocation: Location? = null

    private lateinit var profilebutton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Initializing views
        mLatitude = findViewById<TextView>(R.id.latitude)
        mLongitude = findViewById<TextView>(R.id.longitude)
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        profilebutton= findViewById(R.id.profile)

        profilebutton.setOnClickListener{
            val intent = Intent (this@MapActivity,connection::class.java)
            val UserID = this.intent.getStringExtra("UserID")
            intent.putExtra("UserID",UserID)
            startActivity(intent)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        val permissionState2 = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        return (permissionState == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "entered onRequest")
        when (requestCode) {
            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "about to emter getLastLocation from onRequest")
                    getLastLocation()
                } else {
                    Log.i(TAG, "failed")
                    // wont open
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }




    private fun getLastLocation() {

        Log.i(TAG, "into getLastLocation")
        // error is right here, cannot get location
      //  mFusedLocationClient!!.lastLocation.addOnSuccessListener { location: Location? ->
      //      if (location == null) {
      //          Log.i(TAG, "location is null")
       //         currLatitude = 0.0
       //         currLongitude = 0.0
       //     } else {
       //         Log.i(TAG, "location founded")
       //         currLatitude = location?.latitude
        //        currLongitude = location?.longitude
        //    }
       // }
        currLatitude = 4.0
        currLongitude = 10.0
        val currentLocation = LatLng(currLatitude!!, currLongitude!!)
        mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Device Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (!checkPermissions()) {
            Log.i(TAG, "about to request for permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        } else {
            Log.i(TAG, "permission already granted")
            getLastLocation()
        }
    }

    companion object {
        private val TAG = "whosthere"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 1000
    }
}

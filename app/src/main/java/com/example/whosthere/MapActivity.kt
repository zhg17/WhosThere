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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mLocationManager: LocationManager? = null
    private var currLatitude: Double = 0.0
    private var currLongitude: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    protected var mLastLocation: Location? = null
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val intent = intent
        uid = intent.getStringExtra("uid")
        if (uid == null) {
            Log.i(TAG, "uid gotten from currentUser")
            if(uid == null) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                uid = currentUser!!.uid
                Log.i(TAG, "current uid " + uid)
            }
        }
        // Starting background service
        val serviceIntent = Intent(this, BackgroundLocationService::class.java)
        serviceIntent.putExtra("uid", uid)
        startService(serviceIntent)
        // Initializing views
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                Log.i(TAG, "location is null")
                currLatitude = 0.0
                currLongitude = 0.0
            } else {
                Log.i(TAG, "location founded")
                mLastLocation = location
                currLatitude = location.latitude
                currLongitude = location.longitude
                val ref = FirebaseDatabase.getInstance().getReference("Users/" + uid!! + "/location")
                ref.setValue(mLastLocation)
                Log.i(TAG, currLatitude.toBigDecimal().toPlainString())
                val currentLocation = LatLng(currLatitude, currLongitude)
                mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Device Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
            }
        }
      //  currLatitude = 4.0
      //  currLongitude = 10.0
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

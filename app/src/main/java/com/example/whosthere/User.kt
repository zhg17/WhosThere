package com.example.whosthere

import android.location.Location
import android.location.LocationManager

data class User(val uid: String, val email: String, val friends: ArrayList<String> , val location: Location?, val username:String)


package com.example.whosthere

data class User(val uid: String="", val email: String="", val friends: ArrayList<String> =ArrayList(), val location: PlaceCoor=PlaceCoor(0.0,0.0), val username:String="")


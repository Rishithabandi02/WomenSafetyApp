package com.example.safeher.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(private val context : Context) {

    fun isLocationEnabled() : Boolean{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // We already checked permission before calling this
    fun getLastLocation(onResult : (lat: Double, lng : Double) -> Unit, onFailure : () ->Unit){
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        .addOnSuccessListener {  location ->
                if(location != null){
                    onResult(location.latitude, location.longitude)
                }
                else{
                    onFailure()
                }
        }
        .addOnFailureListener {
                onFailure()
        }
    }
}
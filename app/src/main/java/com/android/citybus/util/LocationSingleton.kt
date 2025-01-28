package com.android.citybus.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng

@SuppressLint("MissingPermission")
fun getLocationUser(context: Context): LatLng {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    try {
        var lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        return if (lastKnownLocation != null) {
            LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
        } else {
            LatLng(-23.560372327785654, -46.65070732268567)
        }
    } catch (e: SecurityException) {
        Log.e("Localização", "Erro ao acessar localização: ${e.message}")
        return LatLng(-23.560372327785654, -46.65070732268567)
    }
}
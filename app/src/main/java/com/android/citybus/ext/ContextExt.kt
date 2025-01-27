package com.android.citybus.ext

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

fun Context.isLocationPermissionGranted(): Boolean {
    val checkLocationExact = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val checkLocationApproximate = checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    return if (checkLocationExact == PackageManager.PERMISSION_GRANTED) {
        true
    } else checkLocationApproximate == PackageManager.PERMISSION_GRANTED
}
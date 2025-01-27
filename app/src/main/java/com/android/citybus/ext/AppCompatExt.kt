package com.android.citybus.ext

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.citybus.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

fun AppCompatActivity.replaceFragmentWithAnimation(fragment: Fragment, frameId: Int, addToBackStack: Boolean = false) {
    supportFragmentManager.beginTransaction().apply {
        setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_bottom, 0, R.anim.exit_to_bottom)
        replace(frameId, fragment, fragment.javaClass.simpleName)
        setPrimaryNavigationFragment(fragment)
        if (addToBackStack) {
            addToBackStack(fragment.javaClass.simpleName)
        }
        commit()
    }
}

fun Activity.isLocationPermissionGranted(): Boolean {
    val checkLocationExact = checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    val checkLocationApproximate = checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    return if (checkLocationExact == PackageManager.PERMISSION_GRANTED) {
        true
    } else checkLocationApproximate == PackageManager.PERMISSION_GRANTED
}

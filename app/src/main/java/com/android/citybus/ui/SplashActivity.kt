package com.android.citybus.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.citybus.databinding.ActivitySplashBinding
import com.android.citybus.ui.busesposition.BusesPositionMapActivity
import com.android.citybus.util.LocationSingleton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity(), PermissionCallback {

    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val REQUEST_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        removeActionBar()
        verifyPermissions()
        initializeMapScreen()
    }

    private fun initializeMapScreen() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(3500)
            callNewActivity()
        }
    }

    private fun callNewActivity() {
        Intent(this, BusesPositionMapActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun removeActionBar() {
        supportActionBar?.hide()
    }

    private fun requestPermissions(callback: PermissionCallback) {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        when {
            ActivityCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, coarseLocationPermission) == PackageManager.PERMISSION_GRANTED -> {
                callback.onPermissionGranted()
            }
            shouldShowRequestPermissionRationale(fineLocationPermission) ||
                    shouldShowRequestPermissionRationale(coarseLocationPermission) -> {
                Toast.makeText(this, "Permita a localização para melhor experiência", Toast.LENGTH_LONG).show()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(fineLocationPermission, coarseLocationPermission),
                    REQUEST_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE) {
            var allGranted = true

            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }

            if (allGranted) {
                this.onPermissionGranted()
            } else {
                this.onPermissionDenied()
            }
        }
    }

    private fun verifyPermissions() {
        requestPermissions(object : PermissionCallback {
            override fun onPermissionGranted() {
                Toast.makeText(this@SplashActivity, "Permissões concedidas!", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied() {
                Toast.makeText(this@SplashActivity, "Permissões negadas.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPermissionGranted() {
        println("listener permissão aceita")
    }

    override fun onPermissionDenied() {
        println("listener permissão negada")
    }
}

interface PermissionCallback {
    fun onPermissionGranted()
    fun onPermissionDenied()
}
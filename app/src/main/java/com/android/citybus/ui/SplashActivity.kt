package com.android.citybus.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.citybus.databinding.ActivitySplashBinding
import com.android.citybus.ui.busesposition.BusesPositionMapActivity
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
                Toast.makeText(this, "Ative a localização para melhor experiência", Toast.LENGTH_LONG).show()
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

    private fun verifyPermissions() {
        requestPermissions(object : PermissionCallback {
            override fun onPermissionGranted() {
                Toast.makeText(this@SplashActivity, "Localização permitida!", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied() {
                Toast.makeText(this@SplashActivity, "Localização negada!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPermissionGranted() {
        println("permissão aceita")
    }

    override fun onPermissionDenied() {
        println("permissão negada")
    }
}

interface PermissionCallback {
    fun onPermissionGranted()
    fun onPermissionDenied()
}
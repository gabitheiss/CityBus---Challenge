package com.android.citybus.ui.busesposition

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.citybus.R
import com.android.citybus.databinding.ActivityMapsBinding
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.ext.isLocationPermissionGranted
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.viewModel

class BusesPositionMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel by viewModel<BusesPositionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel.apply {
            getBusesPosition()

            busesPositionLive.observeForever {
                addMarkersInMap(it)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap.apply {
            uiSettings.isZoomControlsEnabled = true
            isMyLocationEnabled = isLocationPermissionGranted()

            moveCamera(CameraUpdateFactory.newLatLng(getLocationUser()))
        }
    }

    private fun addMarkersInMap(busesPosition: BusesPosition) {
        busesPosition.lines.forEach { line ->
            line.vehiclesList.forEach { vehicle ->
                myMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(vehicle.latitude, vehicle.longitude))
                        .title("Linha ${line.completeSignboard} - Prefixo ${vehicle.prefix}")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUser(): LatLng {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
}
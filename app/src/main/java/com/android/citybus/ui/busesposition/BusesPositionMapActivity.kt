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
import com.android.citybus.ext.gone
import com.android.citybus.ext.isLocationPermissionGranted
import com.android.citybus.ext.replaceFragmentWithAnimation
import com.android.citybus.ext.visible
import com.android.citybus.ui.search.SearchFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class BusesPositionMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap

    private var _binding: ActivityMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<BusMarker>

    private val viewModel by viewModel<BusesPositionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        with(binding) {
            viewModel.apply {
                getBusesPosition()
                _busesPositionLive.observeForever { busesPosition ->
                    clusterManager.clearItems()
                    addMarkersInMap(busesPosition)
                    clusterManager.cluster()
                    loadingView.gone()
                    mapViewGroup.visible()
                }
            }

            updateButtonView.setOnClickListener {
                viewModel.getBusesPosition()
                loadingView.visible()
                mapViewGroup.gone()

            }

            searchButtonView.setOnClickListener {
                replaceFragmentWithAnimation(SearchFragment.newInstance(), R.id.container, true)
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

            setupClusterManager()
        }
    }

    private fun addMarkersInMap(busesPosition: BusesPosition) {
        busesPosition.lines.forEach { line ->
            line.vehiclesList.forEach { vehicle ->
                val markerItem = BusMarker(
                    LatLng(vehicle.latitude, vehicle.longitude),
                    "Linha ${line.completeSignboard} - Prefixo ${vehicle.prefix}"
                )
                clusterManager.addItem(markerItem)
            }
        }
    }

    private fun setupClusterManager() {
        clusterManager = ClusterManager<BusMarker>(this, myMap)

        val customRenderer = CustomClusterRenderer(this, myMap, clusterManager)
        clusterManager.renderer = customRenderer

        myMap.apply {
            setOnCameraIdleListener(clusterManager)
            setOnMarkerClickListener(clusterManager)
            setOnInfoWindowClickListener(clusterManager)
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

    inner class BusMarker(private val position: LatLng, private val title: String) : ClusterItem {

        override fun getPosition(): LatLng {
            return position
        }

        override fun getTitle(): String {
            return title
        }

        override fun getSnippet(): String? = null

        override fun getZIndex(): Float? = null
    }
}
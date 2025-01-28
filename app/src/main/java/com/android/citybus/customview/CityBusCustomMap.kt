package com.android.citybus.customview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.citybus.R
import com.android.citybus.databinding.ViewCustomMapBinding
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.domain.model.BusesPositionToLine
import com.android.citybus.ext.isLocationPermissionGranted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager

class CityBusCustomMap @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), OnMapReadyCallback {

    private var _binding: ViewCustomMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var myMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<BusMarker>
    private lateinit var latLong: LatLng

    init {
        _binding = ViewCustomMapBinding.inflate(LayoutInflater.from(context), this, true)
        requireNotNull(_binding).root
    }

    fun initializeMap(activity: AppCompatActivity, latLong: LatLng) {
        this.latLong = latLong
        val mapFragment = activity.supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap.apply {
            uiSettings.isZoomControlsEnabled = true
            isMyLocationEnabled = context.isLocationPermissionGranted()

            moveCamera(CameraUpdateFactory.newLatLng(latLong))

            setupClusterManager()
        }
    }

    private fun setupClusterManager() {
        clusterManager = ClusterManager<BusMarker>(context, myMap)

        val customRenderer = CustomClusterRenderer(context, myMap, clusterManager)
        clusterManager.renderer = customRenderer

        myMap.apply {
            setOnCameraIdleListener(clusterManager)
            setOnMarkerClickListener(clusterManager)
            setOnInfoWindowClickListener(clusterManager)
        }
    }

    fun addMarkersAllBusesInMap(busesPosition: BusesPosition) {
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

    fun addMarkersBusesLineInMap(busesLinePosition: BusesPositionToLine) {
        busesLinePosition.buses.forEach { line ->
            val markerItem = BusMarker(LatLng(line.latitude, line.longitude), "Linha ${line.prefix}")
            clusterManager.addItem(markerItem)
        }
    }

    fun clearItemsCluster() {
        clusterManager.clearItems()
    }

    fun cluster() {
        clusterManager.cluster()
    }

    fun setTextTitle(textTitle: String) {
        binding.titleView.text = textTitle
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
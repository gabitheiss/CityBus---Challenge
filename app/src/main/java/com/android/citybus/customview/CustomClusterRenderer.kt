package com.android.citybus.customview

import android.content.Context
import com.android.citybus.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class CustomClusterRenderer(
    private val context: Context,
    private val googleMap: GoogleMap,
    private val clusterManager: ClusterManager<CityBusCustomMap.BusMarker>
) : DefaultClusterRenderer<CityBusCustomMap.BusMarker>(context, googleMap, clusterManager) {

    override fun onBeforeClusterItemRendered(item: CityBusCustomMap.BusMarker, markerOptions: MarkerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<CityBusCustomMap.BusMarker>): Boolean = cluster.size > 2
}
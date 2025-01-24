package com.android.citybus.domain.model

import com.google.gson.annotations.SerializedName

data class BusesPosition(
    @SerializedName("hr")
    val hour: String,
    @SerializedName("l")
    val lines: List<Lines>
)

data class Lines(
    @SerializedName("c")
    val completeSignboard: String,
    @SerializedName("cl")
    val codeLine: Int,
    @SerializedName("sl")
    val operationSense: Int,
    @SerializedName("lt0")
    val signboardDestination: String,
    @SerializedName("lt1")
    val signboardOrigin: String,
    @SerializedName("qv")
    val quantityOfVehicles: Int,
    @SerializedName("vs")
    val vehiclesList: List<Vehicles>
)

data class Vehicles(
    @SerializedName("p")
    val prefix: Int,
    @SerializedName("a")
    val withAccessibility: Boolean,
    @SerializedName("ta")
    val locationHour: String,
    @SerializedName("py")
    val latitude: Double,
    @SerializedName("px")
    val longitude: Double
)
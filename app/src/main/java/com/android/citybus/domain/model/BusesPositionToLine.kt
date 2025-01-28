package com.android.citybus.domain.model

import com.google.gson.annotations.SerializedName

data class BusesPositionToLine(
    val hr: String,
    @SerializedName("vs")
    val buses: List<BusPositionToLine>
)

data class BusPositionToLine(
    @SerializedName("p")
    val prefix: String,
    @SerializedName("a")
    val isAccessibility: Boolean,
    @SerializedName("ta")
    val hourLocation: String,
    @SerializedName("py")
    val latitude: Double,
    @SerializedName("px")
    val longitude: Double,
)

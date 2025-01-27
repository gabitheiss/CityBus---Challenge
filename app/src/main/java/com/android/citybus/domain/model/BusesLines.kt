package com.android.citybus.domain.model

import com.google.gson.annotations.SerializedName

data class BusesLines(
    @SerializedName("cl")
    val codeLine: Int,
    @SerializedName("lc")
    val isCircularLine: Boolean,
    @SerializedName("lt")
    val firstLabel: String,
    @SerializedName("tl")
    val secondLabel: String,
    @SerializedName("sl")
    val directionLine: Int,
   @SerializedName("tp")
    val principalTerminal: String,
    @SerializedName("ts")
    val secondaryTerminal: String
)

enum class DirectionLine(val directionLine: Int) {
    PRINCIPAL_TERMINAL(1),
    SECONDARY_TERMINAL(2)
}
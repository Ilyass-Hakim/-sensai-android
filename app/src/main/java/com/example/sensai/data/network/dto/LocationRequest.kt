package com.example.sensai.data.network.dto

import com.google.gson.annotations.SerializedName

data class LocationRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

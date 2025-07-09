package com.example.trainboard

import kotlinx.serialization.Serializable

@Serializable
data class Journey(
    val originStation: Station,
    val destinationStation: Station,
    val departureTime: String

)

@Serializable
data class Station(
    val displayName: String,
    val crs: String
)

@Serializable
data class FareResult(
    val outboundJourneys: List<Journey>
)
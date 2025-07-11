package com.example.trainboard

import kotlinx.serialization.Serializable

@Serializable
data class Journey(
    val originStation: Station,
    val destinationStation: Station,
    val departureTime: String,
    val arrivalTime: String,
    val tickets: List<Ticket>,
    val journeyDurationInMinutes: String
)

@Serializable
data class Station(
    val displayName: String,
    val crs: String
)

@Serializable
data class Ticket(
    val name: String,
    val priceInPennies: String
)



@Serializable
data class FareResult(
    val outboundJourneys: List<Journey>,
)

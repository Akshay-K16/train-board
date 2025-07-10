package com.example.trainboard

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import android.icu.util.Calendar
import io.ktor.client.call.body
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale

class ApiClient {
    val baseURL = "https://int-test1.tram.softwire-lner-dev.co.uk/v1/"
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(HttpCache)

        defaultRequest {
            header(
                "X-API-KEY",
                BuildConfig.API_KEY
            )
        }
    }

    fun getFaresUrl(originStation: String, destinationStation: String): String {
        val time = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.UK)
        val outboundTime = formatter.format(time)
        val url = baseURL + "fares?originStation=$originStation&destinationStation=$destinationStation&noChanges=false&numberOfAdults=1&numberOfChildren=0&journeyType=single&outboundDateTime=$outboundTime&outboundIsArriveBy=false"

        return url
    }


    suspend fun getFares(originCrs: String, destinationCrs: String): FareResult {
        val response: HttpResponse = client.get(getFaresUrl(originCrs, destinationCrs))
        val responseBody: FareResult = response.body()
        return responseBody
    }
}
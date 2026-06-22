package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SunriseSunsetResponse(
    @Json(name = "results") val results: SunriseSunsetResults?,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class SunriseSunsetResults(
    @Json(name = "sunrise") val sunrise: String, // UTC ISO-8601 string, e.g. "2026-06-22T13:17:21+00:00"
    @Json(name = "sunset") val sunset: String,
    @Json(name = "solar_noon") val solarNoon: String,
    @Json(name = "day_length") val dayLength: Long
)

interface SunriseSunsetService {
    @GET("json")
    suspend fun getTimes(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("formatted") formatted: Int = 0,
        @Query("date") date: String = "today"
    ): SunriseSunsetResponse
}

object SunriseSunsetApi {
    private const val BASE_URL = "https://api.sunrise-sunset.org/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: SunriseSunsetService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SunriseSunsetService::class.java)
    }
}

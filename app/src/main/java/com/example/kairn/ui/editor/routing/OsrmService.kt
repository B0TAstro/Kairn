package com.example.kairn.ui.editor.routing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.osmdroid.util.GeoPoint
import java.util.UUID
import kotlin.math.roundToInt

class OsrmService {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://router.project-osrm.org"

    suspend fun calculateRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
    ): List<GeoPoint> {
        val url = "$baseUrl/route/v1/foot/$startLon,$startLat;$endLon,$endLat?overview=full&geometries=geojson"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response from OSRM")
        
        if (!response.isSuccessful) {
            throw Exception("OSRM API error: ${response.code} - $body")
        }
        
        val routeResponse = json.decodeFromString<OsrmResponse>(body)
        
        if (routeResponse.code != "Ok" || routeResponse.routes.isEmpty()) {
            throw Exception("No route found: ${routeResponse.message}")
        }
        
        val geometry = routeResponse.routes.first().geometry
        return geometry.coordinates.map { (lon, lat) -> GeoPoint(lat, lon) }
    }
}

@Serializable
data class OsrmResponse(
    val code: String,
    val message: String? = null,
    val routes: List<OsrmRoute>,
)

@Serializable
data class OsrmRoute(
    val geometry: OsrmGeometry,
)

@Serializable
data class OsrmGeometry(
    val coordinates: List<List<Double>>,
)
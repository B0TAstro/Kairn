package com.example.kairn.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents a resolved user location with coordinates and an optional city name.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "",
)

/**
 * Service that encapsulates GPS location updates and reverse-geocoding.
 * Exposed as a [Flow] of [UserLocation] so the ViewModel can collect lifecycle-aware.
 */
@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val NETWORK_MIN_TIME_MS = 15_000L
        private const val NETWORK_MIN_DISTANCE_M = 50f
        private const val GPS_MIN_TIME_MS = 30_000L
        private const val GPS_MIN_DISTANCE_M = 100f
        private const val PASSIVE_MIN_TIME_MS = 60_000L
        private const val PASSIVE_MIN_DISTANCE_M = 100f
        private const val GEOCODE_MIN_INTERVAL_MS = 120_000L
        private const val GEOCODE_MIN_DISTANCE_M = 250f
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Emits [UserLocation] whenever the device location changes.
     * Immediately emits the last known location (if available) for a fast first render.
     * Automatically cleans up the GPS listener when the collector is cancelled.
     */
    fun locationUpdates(): Flow<UserLocation> = callbackFlow {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = LocationListener { location ->
            trySend(location)
        }

        try {
            // Emit last known position immediately
            val lastKnown =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            lastKnown?.let { trySend(it) }

            val provider = when {
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER) ->
                    LocationManager.PASSIVE_PROVIDER
                else -> null
            }
            provider?.let {
                val minTimeMs = when (it) {
                    LocationManager.NETWORK_PROVIDER -> NETWORK_MIN_TIME_MS
                    LocationManager.GPS_PROVIDER -> GPS_MIN_TIME_MS
                    else -> PASSIVE_MIN_TIME_MS
                }
                val minDistanceM = when (it) {
                    LocationManager.NETWORK_PROVIDER -> NETWORK_MIN_DISTANCE_M
                    LocationManager.GPS_PROVIDER -> GPS_MIN_DISTANCE_M
                    else -> PASSIVE_MIN_DISTANCE_M
                }
                locationManager.requestLocationUpdates(it, minTimeMs, minDistanceM, listener)
            }
        } catch (_: SecurityException) {
            // Permission was revoked between check and request — close silently
        }

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }
        .flowOn(Dispatchers.Main) // LocationManager must be called on main/looper thread
        .conflate()
        .reverseGeocode()

    /**
     * Maps raw [Location] emissions to [UserLocation] with a resolved city name.
     */
    private fun Flow<Location>.reverseGeocode(): Flow<UserLocation> =
        flow {
            var lastGeocodedLocation: Location? = null
            var lastCityName = ""
            var lastGeocodeAt = 0L

            this@reverseGeocode.collect { location ->
                val now = System.currentTimeMillis()
                val previousGeocodedLocation = lastGeocodedLocation
                val shouldGeocode = previousGeocodedLocation == null ||
                    location.distanceTo(previousGeocodedLocation) >= GEOCODE_MIN_DISTANCE_M ||
                    now - lastGeocodeAt >= GEOCODE_MIN_INTERVAL_MS

                if (shouldGeocode) {
                    lastCityName = withContext(Dispatchers.IO) {
                        resolveCity(location.latitude, location.longitude)
                    }
                    lastGeocodedLocation = location
                    lastGeocodeAt = now
                }

                emit(
                    UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        cityName = lastCityName,
                    ),
                )
            }
        }

    @Suppress("DEPRECATION")
    private fun resolveCity(latitude: Double, longitude: Double): String {
        return try {
            val addresses = Geocoder(context, Locale.getDefault())
                .getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                val country = addr.countryName ?: ""
                when {
                    city.isNotBlank() && country.isNotBlank() -> "$city, $country"
                    city.isNotBlank() -> city
                    else -> country
                }
            } else ""
        } catch (_: Exception) {
            ""
        }
    }
}

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
import kotlinx.coroutines.flow.callbackFlow
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
            lastKnown?.let { trySend(it) }

            // Request live updates (every 5 s or 10 m)
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                else -> null
            }
            provider?.let {
                locationManager.requestLocationUpdates(it, 5_000L, 10f, listener)
            }
        } catch (_: SecurityException) {
            // Permission was revoked between check and request — close silently
        }

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }
        .flowOn(Dispatchers.Main) // LocationManager must be called on main/looper thread
        .reverseGeocode()

    /**
     * Maps raw [Location] emissions to [UserLocation] with a resolved city name.
     */
    private fun Flow<Location>.reverseGeocode(): Flow<UserLocation> =
        kotlinx.coroutines.flow.flow {
            this@reverseGeocode.collect { location ->
                val cityName = withContext(Dispatchers.IO) {
                    resolveCity(location.latitude, location.longitude)
                }
                emit(
                    UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        cityName = cityName,
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

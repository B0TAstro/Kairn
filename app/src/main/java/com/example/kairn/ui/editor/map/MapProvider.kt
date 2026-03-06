package com.example.kairn.ui.editor.map

import android.content.Context
import android.view.View
import org.osmdroid.util.GeoPoint

interface MapProvider {
    fun createMapView(context: Context): MapViewWrapper

    fun addPoint(lat: Double, lon: Double, id: String)
    fun removePoint(id: String)
    fun drawRoute(points: List<GeoPoint>, routeId: String = "default")
    fun clearRoutes()
    fun setOnMapClickListener(listener: (lat: Double, lon: Double) -> Unit)
    fun centerMap(lat: Double, lon: Double, zoom: Double? = null)

    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroy()
}

class MapViewWrapper(
    val view: View,
    val type: MapProviderType,
)

enum class MapProviderType {
    OSMDROID,
    MAPBOX,
    MAPLIBRE,
}

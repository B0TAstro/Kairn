package com.example.kairn.ui.editor.map

import android.content.Context
import org.osmdroid.util.GeoPoint

interface MapProvider {
    fun createMapView(context: Context): MapViewWrapper
    
    fun addPoint(lat: Double, lon: Double, id: String)
    fun removePoint(id: String)
    fun drawRoute(points: List<GeoPoint>)
    fun clearRoutes()
    fun setOnMapClickListener(listener: (lat: Double, lon: Double) -> Unit)
    fun centerMap(lat: Double, lon: Double, zoom: Double? = null)
}

class MapViewWrapper(
    val view: Any,
    val type: MapProviderType,
)

enum class MapProviderType {
    OSMDROID,
    MAPLIBRE,
}
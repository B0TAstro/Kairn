package com.example.kairn.ui.editor.map

import android.content.Context
import android.graphics.Color
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.UUID

class OsmMapProvider : MapProvider {
    private lateinit var mapView: MapView
    private var mapClickListener: ((lat: Double, lon: Double) -> Unit)? = null
    private val markers = mutableMapOf<String, Marker>()
    private val routes = mutableMapOf<String, Polyline>()

    override fun createMapView(context: Context): MapViewWrapper {
        Configuration.getInstance().userAgentValue = context.packageName
        mapView = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(14.0)
            
            setOnClickListener { _, geoPoint ->
                mapClickListener?.invoke(geoPoint.latitude, geoPoint.longitude)
                true
            }
        }
        
        return MapViewWrapper(mapView, MapProviderType.OSMDROID)
    }

    override fun addPoint(lat: Double, lon: Double, id: String) {
        val marker = Marker(mapView).apply {
            position = GeoPoint(lat, lon)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = id
            isDraggable = true
        }
        
        mapView.overlays.add(marker)
        markers[id] = marker
        mapView.invalidate()
    }

    override fun removePoint(id: String) {
        markers[id]?.let { marker ->
            mapView.overlays.remove(marker)
            markers.remove(id)
            mapView.invalidate()
        }
    }

    override fun drawRoute(points: List<GeoPoint>, routeId: String) {
        routes[routeId]?.let { existingRoute ->
            mapView.overlays.remove(existingRoute)
        }
        
        val route = Polyline(mapView).apply {
            setPoints(points)
            color = Color.parseColor("#587B6C")
            width = 10f
        }
        
        mapView.overlays.add(route)
        routes[routeId] = route
        mapView.invalidate()
    }

    override fun clearRoutes() {
        routes.values.forEach { route ->
            mapView.overlays.remove(route)
        }
        routes.clear()
        mapView.invalidate()
    }

    override fun setOnMapClickListener(listener: (lat: Double, lon: Double) -> Unit) {
        mapClickListener = listener
    }

    override fun centerMap(lat: Double, lon: Double, zoom: Double?) {
        mapView.controller.animateTo(GeoPoint(lat, lon))
        zoom?.let { mapView.controller.setZoom(it) }
    }
}
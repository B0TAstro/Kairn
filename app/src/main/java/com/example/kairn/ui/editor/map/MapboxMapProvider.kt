package com.example.kairn.ui.editor.map

import android.content.Context
import com.example.kairn.BuildConfig
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import org.osmdroid.util.GeoPoint

class MapboxMapProvider : MapProvider {
    private lateinit var mapView: MapView
    private var mapClickListener: ((lat: Double, lon: Double) -> Unit)? = null
    private var mapboxClickListener: OnMapClickListener? = null

    private var pointManager: PointAnnotationManager? = null
    private var routeManager: PolylineAnnotationManager? = null

    private val markers = mutableMapOf<String, PointAnnotation>()
    private val routes = mutableMapOf<String, PolylineAnnotation>()

    override fun createMapView(context: Context): MapViewWrapper {
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        mapView = MapView(context).apply {
            mapboxMap.loadStyle(Style.OUTDOORS) {
                pointManager = this@apply.annotations.createPointAnnotationManager()
                routeManager = this@apply.annotations.createPolylineAnnotationManager()
                centerMap(46.603354, 1.888334, zoom = 11.0)
                attachMapClickListener()
            }
        }

        return MapViewWrapper(mapView, MapProviderType.MAPBOX)
    }

    override fun addPoint(lat: Double, lon: Double, id: String) {
        val manager = pointManager ?: return
        val annotation = manager.create(
            PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lon, lat)),
        )
        markers[id] = annotation
    }

    override fun removePoint(id: String) {
        val manager = pointManager ?: return
        val marker = markers.remove(id) ?: return
        manager.delete(marker)
    }

    override fun drawRoute(points: List<GeoPoint>, routeId: String) {
        val manager = routeManager ?: return
        routes[routeId]?.let { manager.delete(it) }

        val route = manager.create(
            PolylineAnnotationOptions()
                .withPoints(points.map { Point.fromLngLat(it.longitude, it.latitude) })
                .withLineColor("#587B6C")
                .withLineWidth(5.0),
        )
        routes[routeId] = route
    }

    override fun clearRoutes() {
        routeManager?.deleteAll()
        routes.clear()
    }

    override fun setOnMapClickListener(listener: (lat: Double, lon: Double) -> Unit) {
        mapClickListener = listener
        attachMapClickListener()
    }

    override fun centerMap(lat: Double, lon: Double, zoom: Double?) {
        if (!::mapView.isInitialized) return
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(lon, lat))
                .zoom(zoom ?: 13.6)
                .pitch(60.0)
                .bearing(24.0)
                .build(),
        )
    }

    override fun onStart() {
        if (::mapView.isInitialized) {
            mapView.onStart()
        }
    }

    override fun onResume() = Unit

    override fun onPause() = Unit

    override fun onStop() {
        if (::mapView.isInitialized) {
            mapView.onStop()
        }
    }

    override fun onDestroy() {
        if (::mapView.isInitialized) {
            mapView.onDestroy()
        }
    }

    private fun attachMapClickListener() {
        if (!::mapView.isInitialized) return
        mapboxClickListener?.let { mapView.gestures.removeOnMapClickListener(it) }

        val listener = OnMapClickListener { point ->
            mapClickListener?.invoke(point.latitude(), point.longitude())
            true
        }
        mapboxClickListener = listener
        mapView.gestures.addOnMapClickListener(listener)
    }
}

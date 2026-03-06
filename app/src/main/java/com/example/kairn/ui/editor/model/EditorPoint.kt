package com.example.kairn.ui.editor.model

import org.osmdroid.util.GeoPoint

data class EditorPoint(
    val id: String,
    val order: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String = "Point ${order + 1}",
) {
    val geoPoint: GeoPoint
        get() = GeoPoint(latitude, longitude)
}

fun EditorPoint.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

data class EditorRoute(
    val id: String,
    val startPointId: String,
    val endPointId: String,
    val points: List<GeoPoint>,
)
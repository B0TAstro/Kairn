package com.example.kairn.data.parser

import android.util.Log
import com.example.kairn.domain.model.GpxRoute
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "GpxParser"

@Singleton
class GpxParser @Inject constructor() {

    fun parse(gpxContent: String, fileName: String): Result<GpxRoute> {
        return try {
            Log.d(TAG, "parse: starting parse of $fileName (${gpxContent.length} chars)")

            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(gpxContent.byteInputStream())

            val routeName = extractRouteName(document, fileName)
            val trackPoints = extractTrackPoints(document)
            val createdAt = extractCreatedAt(document)
            val distanceMeters = calculateDistance(trackPoints)

            Log.d(TAG, "parse: $fileName extracted ${trackPoints.size} track points, distance: ${distanceMeters}m")
            Result.success(
                GpxRoute(
                    name = routeName,
                    points = trackPoints,
                    createdAt = createdAt,
                    distanceMeters = distanceMeters,
                    fileName = fileName,
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "parse: error parsing $fileName", e)
            Result.failure(e)
        }
    }

    private fun extractCreatedAt(document: org.w3c.dom.Document): String? {
        val metadata = document.getElementsByTagName("metadata")
        if (metadata.length > 0) {
            val metaElement = metadata.item(0) as? org.w3c.dom.Element
            val timeElements = metaElement?.getElementsByTagName("time")
            if (timeElements?.length ?: 0 > 0) {
                return timeElements!!.item(0).textContent
            }
        }
        return null
    }

    private fun extractRouteName(document: org.w3c.dom.Document, fileName: String): String {
        val metadata = document.getElementsByTagName("metadata")
        if (metadata.length > 0) {
            val nameElements = (metadata.item(0) as? org.w3c.dom.Element)
                ?.getElementsByTagName("name")
            if (nameElements?.length ?: 0 > 0) {
                return nameElements!!.item(0).textContent ?: fileName
            }
        }

        val trk = document.getElementsByTagName("trk")
        if (trk.length > 0) {
            val trkElement = trk.item(0) as? org.w3c.dom.Element
            val nameElements = trkElement?.getElementsByTagName("name")
            if (nameElements?.length ?: 0 > 0) {
                return nameElements!!.item(0).textContent ?: fileName
            }
        }

        return fileName.removeSuffix(".gpx")
    }

    private fun extractTrackPoints(document: org.w3c.dom.Document): List<GeoPoint> {
        val points = mutableListOf<GeoPoint>()

        val trksegs = document.getElementsByTagName("trkseg")
        for (i in 0 until trksegs.length) {
            val trkseg = trksegs.item(i) as? org.w3c.dom.Element ?: continue
            val trkpts = trkseg.getElementsByTagName("trkpt")

            for (j in 0 until trkpts.length) {
                val trkpt = trkpts.item(j) as? org.w3c.dom.Element ?: continue
                val lat = trkpt.getAttribute("lat").toDoubleOrNull()
                val lon = trkpt.getAttribute("lon").toDoubleOrNull()

                if (lat != null && lon != null) {
                    points.add(GeoPoint(lat, lon))
                }
            }
        }

        return points
    }

    private fun calculateDistance(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += haversineDistance(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude
            )
        }
        return totalDistance
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}

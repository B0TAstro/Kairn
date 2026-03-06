package com.example.kairn.data.parser

import android.util.Log
import com.example.kairn.domain.model.GpxRoute
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

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

            Log.d(TAG, "parse: $fileName extracted ${trackPoints.size} track points")
            Result.success(GpxRoute(name = routeName, points = trackPoints))
        } catch (e: Exception) {
            Log.e(TAG, "parse: error parsing $fileName", e)
            Result.failure(e)
        }
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
}

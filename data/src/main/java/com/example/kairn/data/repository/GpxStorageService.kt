package com.example.kairn.data.repository

import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpxStorageService @Inject constructor(
    private val storage: Storage,
) {
    private val bucketName = "GPX_FILES"

    suspend fun uploadGpx(
        points: List<GpxPointPayload>,
        routes: List<List<GpxRoutePointPayload>>,
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val gpxContent = generateGpxContent(points, routes)
            val fileName = generateFileName()

            storage.from(bucketName).upload(
                path = fileName,
                data = gpxContent.encodeToByteArray(),
            )

            val publicUrl = storage.from(bucketName).publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val uuid = UUID.randomUUID().toString().take(8)
        return "${timestamp}_${uuid}.gpx"
    }

    private fun generateGpxContent(
        points: List<GpxPointPayload>,
        routes: List<List<GpxRoutePointPayload>>,
    ): String {
        val builder = StringBuilder()

        builder.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        builder.appendLine("<gpx version=\"1.1\" creator=\"Kairn Editor\" xmlns=\"http://www.topografix.com/GPX/1/1\">")
        builder.appendLine("  <metadata>")
        builder.appendLine("    <name>Kairn Route</name>")
        builder.appendLine("    <desc>Route created with Kairn hiking app</desc>")
        builder.appendLine("    <time>${formatIsoDateTime(Date())}</time>")
        builder.appendLine("  </metadata>")

        if (points.isNotEmpty()) {
            builder.appendLine("  <wpt lat=\"${points.first().latitude}\" lon=\"${points.first().longitude}\">")
            builder.appendLine("    <name>Start</name>")
            builder.appendLine("    <sym>Flag</sym>")
            builder.appendLine("  </wpt>")

            if (points.size > 1) {
                builder.appendLine("  <wpt lat=\"${points.last().latitude}\" lon=\"${points.last().longitude}\">")
                builder.appendLine("    <name>End</name>")
                builder.appendLine("    <sym>Flag</sym>")
                builder.appendLine("  </wpt>")
            }

            points.forEachIndexed { index, point ->
                if (index > 0 && index < points.size - 1) {
                    builder.appendLine("  <wpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">")
                    builder.appendLine("    <name>${point.name}</name>")
                    builder.appendLine("    <sym>Waypoint</sym>")
                    builder.appendLine("  </wpt>")
                }
            }
        }

        if (routes.isNotEmpty()) {
            builder.appendLine("  <trk>")
            builder.appendLine("    <name>Kairn Route</name>")
            builder.appendLine("    <trkseg>")

            routes.flatMap { it }.forEach { point ->
                builder.appendLine("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\" />")
            }

            builder.appendLine("    </trkseg>")
            builder.appendLine("  </trk>")
        }

        builder.appendLine("</gpx>")

        return builder.toString()
    }

    private fun formatIsoDateTime(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return format.format(date)
    }
}

data class GpxPointPayload(
    val latitude: Double,
    val longitude: Double,
    val name: String,
)

data class GpxRoutePointPayload(
    val latitude: Double,
    val longitude: Double,
)

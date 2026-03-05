package com.example.kairn.ui.editor.gpx

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.kairn.ui.editor.model.EditorPoint
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GpxExporter(private val context: Context) {
    
    fun exportToFile(points: List<EditorPoint>, routes: List<List<GeoPoint>>): Uri? {
        return try {
            val gpxContent = generateGpxContent(points, routes)
            val fileName = "kairn_route_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.gpx"
            val cacheDir = context.cacheDir
            val gpxFile = File(cacheDir, fileName)
            
            FileOutputStream(gpxFile).use { outputStream ->
                outputStream.write(gpxContent.toByteArray())
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                gpxFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun generateGpxContent(points: List<EditorPoint>, routes: List<List<GeoPoint>>): String {
        val builder = StringBuilder()
        
        builder.appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        builder.appendLine("<gpx version=\"1.1\" creator=\"Kairn Editor\" xmlns=\"http://www.topografix.com/GPX/1/1\">")
        builder.appendLine("  <metadata>")
        builder.appendLine("    <name>Kairn Route</name>")
        builder.appendLine("    <desc>Route created with Kairn Editor</desc>")
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
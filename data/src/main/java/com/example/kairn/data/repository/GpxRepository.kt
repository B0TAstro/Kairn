package com.example.kairn.data.repository

import android.util.Log
import com.example.kairn.BuildConfig
import com.example.kairn.data.remote.HikeDto
import com.example.kairn.domain.model.GpxRoute
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GpxRepository"

data class GpxFileInfo(
    val name: String,
    val publicUrl: String,
)

data class GpxHikeMetadata(
    val id: String,
    val creatorId: String?,
    val createdAt: String,
)

@Singleton
class GpxRepository @Inject constructor(
    private val storage: Storage,
    private val auth: Auth,
    private val postgrest: Postgrest,
) {
    private val bucketName = "GPX_FILES"
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun buildDownloadUrl(fileName: String): String {
        val supabaseUrl = BuildConfig.SUPABASE_URL.removeSuffix("/")
        return "$supabaseUrl/storage/v1/object/$bucketName/$fileName"
    }

    suspend fun listGpxFiles(): Result<List<GpxFileInfo>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val bucket = storage.from(bucketName)
            val files = bucket.list()

            Log.d(TAG, "listGpxFiles: found ${files.size} items in bucket")

            val gpxFiles = files.mapNotNull { item ->
                if (item.name.endsWith(".gpx", ignoreCase = true)) {
                    GpxFileInfo(
                        name = item.name,
                        publicUrl = buildDownloadUrl(item.name),
                    )
                } else null
            }

            Log.d(TAG, "listGpxFiles: found ${gpxFiles.size} GPX files")
            Result.success(gpxFiles)
        } catch (e: Exception) {
            Log.e(TAG, "listGpxFiles: error", e)
            Result.failure(e)
        }
    }

    private suspend fun getJwtToken(): String {
        return try {
            val session = auth.sessionStatus.firstOrNull()
            session?.let {
                when (it) {
                    is io.github.jan.supabase.auth.status.SessionStatus.Authenticated -> it.session.accessToken
                    else -> ""
                }
            } ?: ""
        } catch (e: Exception) {
            Log.w(TAG, "getJwtToken: could not get JWT token", e)
            ""
        }
    }

    suspend fun downloadGpxContent(publicUrl: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "downloadGpxContent: starting download from $publicUrl")

            val jwtToken = getJwtToken()

            val requestBuilder = Request.Builder()
                .url(publicUrl)

            if (jwtToken.isNotEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $jwtToken")
                Log.d(TAG, "downloadGpxContent: using JWT token")
            } else {
                Log.w(TAG, "downloadGpxContent: no JWT token available")
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                Log.e(TAG, "downloadGpxContent: HTTP ${response.code} - $responseBody")
                throw Exception("HTTP ${response.code}: ${responseBody ?: "Unknown error"}")
            }

            if (responseBody.isNullOrBlank()) {
                throw Exception("Empty response body")
            }

            if (responseBody.trimStart().startsWith("{")) {
                Log.e(TAG, "downloadGpxContent: got JSON error instead of GPX: $responseBody")
                throw Exception("Got JSON error response: $responseBody")
            }

            Log.d(TAG, "downloadGpxContent: downloaded ${responseBody.length} characters (GPX content)")
            Result.success(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "downloadGpxContent: error downloading", e)
            Result.failure(e)
        }
    }

    suspend fun saveGpxToHikes(gpxRoute: GpxRoute, creatorId: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "saveGpxToHikes: saving ${gpxRoute.name} to hikes table")

            val distanceMeters = gpxRoute.distanceMeters?.toInt() ?: 0

            val hikeData = mapOf(
                "creator_id" to creatorId,
                "title" to gpxRoute.name,
                "distance_m" to distanceMeters,
                "gpx_filename" to gpxRoute.fileName,
                "status" to "draft",
            )

            val result = postgrest["hikes"].insert(hikeData)
            Log.d(TAG, "saveGpxToHikes: saved successfully")
            Result.success("Saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "saveGpxToHikes: error", e)
            Result.failure(e)
        }
    }

    suspend fun getHikesMetadata(): Result<Map<String, GpxHikeMetadata>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "getHikesMetadata: fetching hikes from Supabase")

            val hikes = postgrest["hikes"]
                .select()
                .decodeList<HikeDto>()

            Log.d(TAG, "getHikesMetadata: found ${hikes.size} hikes")

            // Filtrer les hikes qui ont un gpx_filename et créer une map
            val hikesMap = hikes
                .filter { it.gpxFilename != null }
                .associate { dto ->
                    dto.gpxFilename!! to GpxHikeMetadata(
                        id = dto.id,
                        creatorId = dto.creatorId,
                        createdAt = dto.createdAt,
                    )
                }

            Log.d(TAG, "getHikesMetadata: ${hikesMap.size} hikes with gpx_filename")
            Result.success(hikesMap)
        } catch (e: Exception) {
            Log.e(TAG, "getHikesMetadata: error", e)
            Result.failure(e)
        }
    }
}

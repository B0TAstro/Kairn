package com.example.kairn.data.repository

import com.example.kairn.data.remote.HikeDto
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.repository.HikeRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HikeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : HikeRepository {

    override suspend fun getHikes(): Result<List<Hike>> = runCatching {
        postgrest
            .from("hikes")
            .select()
            .decodeList<HikeDto>()
            .map { it.toDomain() }
    }

    override suspend fun getHikeById(hikeId: String): Result<Hike> = runCatching {
        postgrest
            .from("hikes")
            .select { filter { eq("id", hikeId) } }
            .decodeSingle<HikeDto>()
            .toDomain()
    }

    /**
     * Fetches the user's completed hikes by joining `hike_runs` → `hikes`.
     * Returns an empty list if the `hike_runs` table doesn't exist yet.
     */
    override suspend fun getCompletedHikes(userId: String): Result<List<Hike>> = try {
        val hikes = postgrest
            .from("hike_runs")
            .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("hike_id, hikes(*)")) {
                filter {
                    eq("user_id", userId)
                    eq("status", "completed")
                }
            }
            .decodeList<CompletedRunWithHike>()
            .mapNotNull { it.hike?.toDomain() }
        Result.success(hikes)
    } catch (_: Exception) {
        // Table doesn't exist yet or query failed — return empty list gracefully
        Result.success(emptyList())
    }
}

/**
 * Helper DTO for the `hike_runs` joined query.
 * Decodes `hike_runs.hike_id` + the embedded `hikes(*)` relation.
 */
@kotlinx.serialization.Serializable
private data class CompletedRunWithHike(
    @kotlinx.serialization.SerialName("hike_id") val hikeId: String,
    @kotlinx.serialization.SerialName("hikes") val hike: HikeDto? = null,
)

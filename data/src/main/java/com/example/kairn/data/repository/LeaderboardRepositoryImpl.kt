package com.example.kairn.data.repository

import com.example.kairn.data.remote.LeaderboardEntryDto
import com.example.kairn.data.remote.RegionDto
import com.example.kairn.domain.model.LeaderboardEntry
import com.example.kairn.domain.repository.LeaderboardRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : LeaderboardRepository {

    companion object {
        private const val LEADERBOARD_VIEW = "leaderboard_view"
        private const val REGIONS_TABLE = "regions"
    }

    override suspend fun getRegionalLeaderboard(
        regionId: Long,
        currentUserId: String,
    ): Result<List<LeaderboardEntry>> = runCatching {
        val dtos = postgrest
            .from(LEADERBOARD_VIEW)
            .select {
                filter { eq("region_id", regionId) }
                order("total_xp", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<LeaderboardEntryDto>()
        dtos.toLeaderboardEntries(currentUserId)
    }

    override suspend fun getNationalLeaderboard(
        countryCode: String,
        currentUserId: String,
    ): Result<List<LeaderboardEntry>> = runCatching {
        val dtos = postgrest
            .from(LEADERBOARD_VIEW)
            .select {
                filter { eq("country_code", countryCode) }
                order("total_xp", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<LeaderboardEntryDto>()
        dtos.toLeaderboardEntries(currentUserId)
    }

    override suspend fun getGlobalLeaderboard(
        currentUserId: String,
    ): Result<List<LeaderboardEntry>> = runCatching {
        val dtos = postgrest
            .from(LEADERBOARD_VIEW)
            .select {
                order("total_xp", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<LeaderboardEntryDto>()
        dtos.toLeaderboardEntries(currentUserId)
    }

    override suspend fun getOrCreateRegion(
        countryCode: String,
        regionName: String,
    ): Result<Long> = runCatching {
        // Try to find existing region
        val existing = postgrest
            .from(REGIONS_TABLE)
            .select {
                filter {
                    eq("country_code", countryCode)
                    eq("name", regionName)
                }
            }
            .decodeSingleOrNull<RegionDto>()

        if (existing != null) {
            existing.id
        } else {
            // Insert new region and return its id
            val inserted = postgrest
                .from(REGIONS_TABLE)
                .insert(
                    RegionDto(id = 0, countryCode = countryCode, name = regionName),
                ) {
                    select()
                }
                .decodeSingle<RegionDto>()
            inserted.id
        }
    }

    /**
     * Converts a list of DTOs into domain [LeaderboardEntry] objects,
     * assigning 1-based ranks by list position.
     */
    private fun List<LeaderboardEntryDto>.toLeaderboardEntries(
        currentUserId: String,
    ): List<LeaderboardEntry> = mapIndexed { index, dto ->
        LeaderboardEntry(
            rank = index + 1,
            userId = dto.userId,
            username = dto.username ?: "Anonyme",
            avatarUrl = dto.avatarUrl,
            level = dto.level,
            xp = dto.totalXp,
            isCurrentUser = dto.userId == currentUserId,
        )
    }
}

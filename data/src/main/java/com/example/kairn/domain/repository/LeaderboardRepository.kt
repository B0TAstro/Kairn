package com.example.kairn.domain.repository

import com.example.kairn.domain.model.LeaderboardEntry

/**
 * Provides leaderboard data at three scopes: regional, national and global.
 *
 * All methods return the full sorted list; the ViewModel is responsible for
 * windowing (e.g. 5 above / current user / 5 below).
 */
interface LeaderboardRepository {

    /**
     * Returns leaderboard entries filtered by [regionId].
     */
    suspend fun getRegionalLeaderboard(
        regionId: Long,
        currentUserId: String,
    ): Result<List<LeaderboardEntry>>

    /**
     * Returns leaderboard entries filtered by [countryCode] (ISO 3166-1 alpha-2).
     */
    suspend fun getNationalLeaderboard(
        countryCode: String,
        currentUserId: String,
    ): Result<List<LeaderboardEntry>>

    /**
     * Returns the global leaderboard (all users, no filter).
     */
    suspend fun getGlobalLeaderboard(
        currentUserId: String,
    ): Result<List<LeaderboardEntry>>

    /**
     * Looks up a region by name and country code.
     * Creates the region if it does not exist yet.
     * Returns the region id.
     */
    suspend fun getOrCreateRegion(
        countryCode: String,
        regionName: String,
    ): Result<Long>
}

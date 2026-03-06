package com.example.kairn.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the Supabase `leaderboard_view` view.
 *
 * View definition (profiles JOIN user_stats):
 * `leaderboard_view`: (`user_id`, `username`, `avatar_url`, `level`, `total_xp`, `region_id`, `country_code`)
 */
@Serializable
data class LeaderboardEntryDto(
    @SerialName("user_id") val userId: String,
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("level") val level: Int = 1,
    @SerialName("total_xp") val totalXp: Long = 0,
    @SerialName("region_id") val regionId: Long? = null,
    @SerialName("country_code") val countryCode: String? = null,
)

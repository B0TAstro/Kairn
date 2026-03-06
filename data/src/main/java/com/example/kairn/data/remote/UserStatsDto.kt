package com.example.kairn.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the Supabase `user_stats` table.
 *
 * Table schema:
 * `user_stats`: (`user_id`, `level`, `total_xp`, `total_distance_m`, `completed_runs_count`)
 */
@Serializable
data class UserStatsDto(
    @SerialName("user_id") val userId: String,
    @SerialName("level") val level: Int = 1,
    @SerialName("total_xp") val totalXp: Long = 0,
    @SerialName("total_distance_m") val totalDistanceM: Long = 0,
    @SerialName("completed_runs_count") val completedRunsCount: Int = 0,
)

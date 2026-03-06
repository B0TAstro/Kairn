package com.example.kairn.data.remote

import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.domain.model.HikeDifficulty
import com.example.kairn.domain.model.HikeStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HikeDto(
    @SerialName("id") val id: String,
    @SerialName("creator_id") val creatorId: String? = null,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("difficulty") val difficulty: String = "moderate",
    @SerialName("estimated_duration_min") val estimatedDurationMin: Int? = null,
    @SerialName("distance_m") val distanceM: Int? = null,
    @SerialName("elevation_gain_m") val elevationGainM: Int? = null,
    @SerialName("recommended_level") val recommendedLevel: Int = 1,
    @SerialName("status") val status: String = "draft",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    @SerialName("location") val location: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category") val category: String? = null,
) {
    fun toDomain(): Hike = Hike(
        id = id,
        creatorId = creatorId ?: "",
        title = title,
        description = description,
        difficulty = when (difficulty.lowercase()) {
            "easy" -> HikeDifficulty.EASY
            "hard" -> HikeDifficulty.HARD
            "expert" -> HikeDifficulty.EXPERT
            else -> HikeDifficulty.MODERATE
        },
        estimatedDurationMin = estimatedDurationMin,
        distanceM = distanceM,
        elevationGainM = elevationGainM,
        recommendedLevel = recommendedLevel,
        status = when (status.lowercase()) {
            "published" -> HikeStatus.PUBLISHED
            "archived" -> HikeStatus.ARCHIVED
            else -> HikeStatus.DRAFT
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
        location = location,
        imageUrl = imageUrl,
        category = when (category?.lowercase()) {
            "mountain" -> HikeCategory.MOUNTAIN
            "forest" -> HikeCategory.FOREST
            "lake" -> HikeCategory.LAKE
            "cave" -> HikeCategory.CAVE
            else -> null
        },
    )
}

package com.example.kairn.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the Supabase `profiles` table.
 *
 * Table schema:
 * `profiles`: (`id`, `username`, `avatar_url`, `bio`, `city_id`, `region_id`, `country_code`, `created_at`)
 */
@Serializable
data class ProfileDto(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("region_id") val regionId: Long? = null,
    @SerialName("country_code") val countryCode: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

/**
 * Payload for updating profile fields.
 * Only non-null fields will be serialized (thanks to default = null + encodeDefaults = false).
 */
@Serializable
data class ProfileUpdateDto(
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
)

/**
 * Payload for updating geolocation fields on a profile.
 */
@Serializable
data class ProfileGeoUpdateDto(
    @SerialName("country_code") val countryCode: String,
    @SerialName("region_id") val regionId: Long,
)

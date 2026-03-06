package com.example.kairn.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO matching the Supabase `regions` table.
 *
 * Table schema:
 * `regions`: (`id`, `country_code`, `name`)
 */
@Serializable
data class RegionDto(
    @SerialName("id") val id: Long,
    @SerialName("country_code") val countryCode: String,
    @SerialName("name") val name: String,
)

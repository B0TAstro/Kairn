package com.example.kairn.domain.model

import org.osmdroid.util.GeoPoint

data class GpxRoute(
    val id: String? = null,
    val name: String,
    val points: List<GeoPoint>,
    val createdAt: String? = null,
    val creatorId: String? = null,
    val distanceMeters: Double? = null,
    val fileName: String? = null,
)

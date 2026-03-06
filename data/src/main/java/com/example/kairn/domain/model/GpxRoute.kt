package com.example.kairn.domain.model

import org.osmdroid.util.GeoPoint

data class GpxRoute(
    val name: String,
    val points: List<GeoPoint>,
)

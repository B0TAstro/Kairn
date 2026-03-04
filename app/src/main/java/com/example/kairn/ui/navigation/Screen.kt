package com.example.kairn.ui.navigation

enum class Screen {
    HOME,
    DETAILS,
    SAVED,
    PROFILE;
}

object NavRoutes {
    const val HIKE_DETAIL = "hike_detail/{hikeId}"
    fun hikeDetail(hikeId: String) = "hike_detail/$hikeId"
}
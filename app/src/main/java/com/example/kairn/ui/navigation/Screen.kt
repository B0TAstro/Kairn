package com.example.kairn.ui.navigation

enum class Screen {
    HOME,
    EXPLORE,
    CHAT,
    PROFILE,
}

object NavRoutes {
    const val HIKE_DETAIL = "hike_detail/{hikeId}"
    fun hikeDetail(hikeId: String) = "hike_detail/$hikeId"

    const val ACCOUNT_HIKE_DETAIL = "account_hike_detail/{hikeId}"
    fun accountHikeDetail(hikeId: String) = "account_hike_detail/$hikeId"

    const val EDIT_PROFILE = "edit_profile"
}

package com.example.kairn.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class Screen {
    HOME,
    EXPLORE,
    EDITOR,
    CHAT,
    PROFILE,
}

object NavRoutes {
    const val HIKE_DETAIL = "hike_detail/{hikeId}"
    fun hikeDetail(hikeId: String) = "hike_detail/$hikeId"

    const val ACCOUNT_HIKE_DETAIL = "account_hike_detail/{hikeId}"
    fun accountHikeDetail(hikeId: String) = "account_hike_detail/$hikeId"

    const val EDIT_PROFILE = "edit_profile"

    // Chat routes
    const val CHAT_LIST = "chat_list"
    const val CHAT = "chat/{conversationId}/{conversationName}"
    const val FRIEND_LIST = "friend_list"

    fun chat(conversationId: String, conversationName: String) =
        "chat/$conversationId/${URLEncoder.encode(conversationName, StandardCharsets.UTF_8.toString())}"
}

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
    
    // Chat routes
    const val CHAT_LIST = "chat_list"
    const val CHAT = "chat/{conversationId}/{conversationName}"
    const val FRIEND_LIST = "friend_list"
    
    fun chat(conversationId: String, conversationName: String) = 
        "chat/$conversationId/$conversationName"
}

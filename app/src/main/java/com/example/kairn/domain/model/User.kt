package com.example.kairn.domain.model

data class User(
    val id: String,
    val email: String,
    val username: String,
    val avatarUrl: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
) {
    companion object {
        val preview = User(
            id = "123",
            email = "john@example.com",
            username = "John Hiker",
            level = 5,
            xp = 1250,
            city = "Paris",
            region = "Île-de-France",
            country = "France"
        )
    }
}
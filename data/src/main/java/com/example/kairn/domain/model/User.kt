package com.example.kairn.domain.model

data class User(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val pseudo: String? = null,
    val username: String? = null,
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
            firstName = "John",
            lastName = "Doe",
            pseudo = "JohnHiker",
            username = "John Hiker",
            level = 5,
            xp = 1250,
            city = "Paris",
            region = "Île-de-France",
            country = "France",
        )
    }
}

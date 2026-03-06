package com.example.kairn.domain.model

data class User(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val pseudo: String? = null,
    val username: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val hikesCompleted: Int = 0,
    val totalDistanceM: Long = 0,
    val longestTrailKm: Double = 0.0,
    val createdAt: String? = null,
    val city: String? = null,
    val region: String? = null,
    val regionId: Long? = null,
    val country: String? = null,
    val countryCode: String? = null,
) {
    companion object {
        val preview = User(
            id = "123",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            pseudo = "JohnHiker",
            username = "John Hiker",
            bio = "Passionné de randonnée depuis toujours",
            level = 5,
            xp = 1250,
            hikesCompleted = 23,
            totalDistanceM = 142_000,
            longestTrailKm = 21.4,
            createdAt = "2023-05-15T10:30:00Z",
            city = "Paris",
            region = "Île-de-France",
            regionId = 1,
            country = "France",
            countryCode = "FR",
        )
    }
}

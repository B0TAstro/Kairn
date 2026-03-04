package com.example.kairn.domain.model

data class Hike(
    val id: String,
    val name: String,
    val location: String,
    val elevationMeters: Int,
    val durationMinutes: Int,
    val distanceKm: Double,
    val difficulty: HikeDifficulty,
    val description: String = "",
    val imageUrl: String? = null,
    val reviewCount: Int = 0,
) {
    val formattedDuration: String
        get() {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            return if (hours > 0 && minutes > 0) "${hours}h ${minutes}min"
            else if (hours > 0) "${hours}h"
            else "${minutes}min"
        }

    val formattedDistance: String
        get() = if (distanceKm >= 1.0) "${"%.1f".format(distanceKm)}km"
        else "${(distanceKm * 1000).toInt()}m"

    val formattedElevation: String
        get() = "${elevationMeters}m"

    companion object {
        val preview = Hike(
            id = "1",
            name = "Aiguille du Midi",
            location = "Chamonix, France",
            elevationMeters = 3842,
            durationMinutes = 630,
            distanceKm = 28.0,
            difficulty = HikeDifficulty.EXPERT,
            description = "Experience one of the most breathtaking adventures in the French Alps as you hike toward the iconic Aiguille du Midi. Located in Chamonix, this trail offers panoramic views of Mont Blanc and the Alps, surrounded by stunning peaks, glaciers, and the majestic Mont Blanc massif.",
            reviewCount = 124,
        )

        val previewList = listOf(
            preview,
            Hike(
                id = "2",
                name = "Lac Blanc",
                location = "Chamonix, France",
                elevationMeters = 2352,
                durationMinutes = 300,
                distanceKm = 12.5,
                difficulty = HikeDifficulty.INTERMEDIATE,
                description = "A classic Chamonix hike with stunning views of the Mont Blanc massif reflected in the crystal-clear alpine lake.",
                reviewCount = 89,
            ),
            Hike(
                id = "3",
                name = "Mer de Glace",
                location = "Chamonix, France",
                elevationMeters = 1913,
                durationMinutes = 180,
                distanceKm = 8.0,
                difficulty = HikeDifficulty.BEGINNER,
                description = "An accessible hike leading to France's largest glacier.",
                reviewCount = 203,
            ),
        )
    }
}

enum class HikeDifficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    EXPERT("Expert"),
}

enum class HikeCategory(val label: String) {
    MOUNTAIN("Mountain"),
    FOREST("Forest"),
    LAKE("Lake"),
    CAVE("Cave"),
}

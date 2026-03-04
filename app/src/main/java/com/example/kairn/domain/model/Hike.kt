package com.example.kairn.domain.model

data class Hike(
    val id: String,
    val name: String,
    val location: String,
    val elevationMeters: Int,
    val durationMinutes: Int,
    val distanceKm: Double,
    val difficulty: HikeDifficulty,
    val category: HikeCategory = HikeCategory.MOUNTAIN,
    val description: String = "",
    val imageUrl: String? = null,
    val reviewCount: Int = 0,
    /** Optional display range label e.g. "10-12h". Falls back to formattedDuration. */
    val durationLabel: String? = null,
) {
    val formattedDuration: String
        get() {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            return if (hours > 0 && minutes > 0) "${hours}h ${minutes}min"
            else if (hours > 0) "${hours}h"
            else "${minutes}min"
        }

    val displayDuration: String get() = durationLabel ?: formattedDuration

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
            category = HikeCategory.MOUNTAIN,
            durationLabel = "10-12h",
            description = "Experience one of the most breathtaking adventures in the French Alps as you hike toward the iconic Aiguille du Midi. Located in Chamonix, France, this trail offers unparalleled views of the surrounding peaks, glaciers, and the majestic Mont Blanc — the highest point in Western Europe.",
            reviewCount = 124,
            imageUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&q=80",
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
                category = HikeCategory.LAKE,
                durationLabel = "5-6h",
                description = "A classic Chamonix hike with stunning views of the Mont Blanc massif reflected in the crystal-clear alpine lake. The trail winds through alpine meadows and offers exceptional panoramas.",
                reviewCount = 89,
                imageUrl = "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=800&q=80",
            ),
            Hike(
                id = "3",
                name = "Mer de Glace",
                location = "Chamonix, France",
                elevationMeters = 1913,
                durationMinutes = 180,
                distanceKm = 8.0,
                difficulty = HikeDifficulty.BEGINNER,
                category = HikeCategory.MOUNTAIN,
                durationLabel = "3h",
                description = "An accessible hike leading to France's largest glacier. A magical journey through ancient ice formations carved over thousands of years.",
                reviewCount = 203,
                imageUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800&q=80",
            ),
            Hike(
                id = "4",
                name = "Grand Balcon Nord",
                location = "Chamonix, France",
                elevationMeters = 2400,
                durationMinutes = 360,
                distanceKm = 15.0,
                difficulty = HikeDifficulty.INTERMEDIATE,
                category = HikeCategory.MOUNTAIN,
                durationLabel = "6-7h",
                description = "A spectacular ridge trail running along the north side of the Chamonix valley with breathtaking views of Mont Blanc and the Aiguilles Rouges.",
                reviewCount = 67,
                imageUrl = "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=800&q=80",
            ),
            Hike(
                id = "5",
                name = "Forêt de Bénévise",
                location = "Servoz, France",
                elevationMeters = 1200,
                durationMinutes = 150,
                distanceKm = 7.5,
                difficulty = HikeDifficulty.BEGINNER,
                category = HikeCategory.FOREST,
                durationLabel = "2-3h",
                description = "A peaceful forest walk through ancient pine and fir trees. Perfect for beginners and families, with natural wildlife and serene atmosphere.",
                reviewCount = 41,
                imageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800&q=80",
            ),
            Hike(
                id = "6",
                name = "Lac Cornu",
                location = "Les Houches, France",
                elevationMeters = 2114,
                durationMinutes = 240,
                distanceKm = 10.0,
                difficulty = HikeDifficulty.INTERMEDIATE,
                category = HikeCategory.LAKE,
                durationLabel = "4-5h",
                description = "A hidden gem tucked between granite peaks, Lac Cornu rewards hikers with turquoise waters and complete solitude far from the crowds.",
                reviewCount = 28,
                imageUrl = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800&q=80",
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

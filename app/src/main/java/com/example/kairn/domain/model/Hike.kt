package com.example.kairn.domain.model

data class Hike(
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String? = null,
    val difficulty: HikeDifficulty = HikeDifficulty.MODERATE,
    val estimatedDurationMin: Int? = null,
    val distanceM: Int? = null,
    val elevationGainM: Int? = null,
    val recommendedLevel: Int = 1,
    val status: HikeStatus = HikeStatus.DRAFT,
    val createdAt: String = "",
    val updatedAt: String = "",
) {
    val formattedDuration: String
        get() {
            val minutes = estimatedDurationMin ?: return "—"
            val hours = minutes / 60
            val mins = minutes % 60
            return if (hours > 0 && mins > 0) "${hours}h ${mins}min"
            else if (hours > 0) "${hours}h"
            else "${mins}min"
        }

    val formattedDistance: String
        get() {
            val m = distanceM ?: return "—"
            return if (m >= 1000) "${"%.1f".format(m / 1000.0)}km"
            else "${m}m"
        }

    val formattedElevation: String
        get() = elevationGainM?.let { "${it}m" } ?: "—"

    companion object {
        val preview = Hike(
            id = "00000000-0000-0000-0000-000000000001",
            creatorId = "00000000-0000-0000-0000-000000000000",
            title = "Aiguille du Midi",
            description = "Experience one of the most breathtaking adventures in the French Alps as you hike toward the iconic Aiguille du Midi. Located in Chamonix, France, this trail offers unparalleled views of the surrounding peaks, glaciers, and the majestic Mont Blanc — the highest point in Western Europe.",
            difficulty = HikeDifficulty.EXPERT,
            estimatedDurationMin = 630,
            distanceM = 28000,
            elevationGainM = 3842,
            recommendedLevel = 5,
            status = HikeStatus.PUBLISHED,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z",
        )

        val previewList = listOf(
            preview,
            Hike(
                id = "00000000-0000-0000-0000-000000000002",
                creatorId = "00000000-0000-0000-0000-000000000000",
                title = "Lac Blanc",
                description = "A classic Chamonix hike with stunning views of the Mont Blanc massif reflected in the crystal-clear alpine lake. The trail winds through alpine meadows and offers exceptional panoramas.",
                difficulty = HikeDifficulty.MODERATE,
                estimatedDurationMin = 300,
                distanceM = 12500,
                elevationGainM = 2352,
                recommendedLevel = 3,
                status = HikeStatus.PUBLISHED,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z",
            ),
            Hike(
                id = "00000000-0000-0000-0000-000000000003",
                creatorId = "00000000-0000-0000-0000-000000000000",
                title = "Mer de Glace",
                description = "An accessible hike leading to France's largest glacier. A magical journey through ancient ice formations carved over thousands of years.",
                difficulty = HikeDifficulty.EASY,
                estimatedDurationMin = 180,
                distanceM = 8000,
                elevationGainM = 1913,
                recommendedLevel = 1,
                status = HikeStatus.PUBLISHED,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z",
            ),
            Hike(
                id = "00000000-0000-0000-0000-000000000004",
                creatorId = "00000000-0000-0000-0000-000000000000",
                title = "Grand Balcon Nord",
                description = "A spectacular ridge trail running along the north side of the Chamonix valley with breathtaking views of Mont Blanc and the Aiguilles Rouges.",
                difficulty = HikeDifficulty.MODERATE,
                estimatedDurationMin = 360,
                distanceM = 15000,
                elevationGainM = 2400,
                recommendedLevel = 3,
                status = HikeStatus.PUBLISHED,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z",
            ),
            Hike(
                id = "00000000-0000-0000-0000-000000000005",
                creatorId = "00000000-0000-0000-0000-000000000000",
                title = "Forêt de Bénévise",
                description = "A peaceful forest walk through ancient pine and fir trees. Perfect for beginners and families, with natural wildlife and serene atmosphere.",
                difficulty = HikeDifficulty.EASY,
                estimatedDurationMin = 150,
                distanceM = 7500,
                elevationGainM = 1200,
                recommendedLevel = 1,
                status = HikeStatus.PUBLISHED,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z",
            ),
            Hike(
                id = "00000000-0000-0000-0000-000000000006",
                creatorId = "00000000-0000-0000-0000-000000000000",
                title = "Lac Cornu",
                description = "A hidden gem tucked between granite peaks, Lac Cornu rewards hikers with turquoise waters and complete solitude far from the crowds.",
                difficulty = HikeDifficulty.MODERATE,
                estimatedDurationMin = 240,
                distanceM = 10000,
                elevationGainM = 2114,
                recommendedLevel = 2,
                status = HikeStatus.PUBLISHED,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z",
            ),
        )
    }
}

// Valeurs alignées sur le type enum Supabase `hike_difficulty`
enum class HikeDifficulty(val label: String) {
    EASY("Easy"),
    MODERATE("Moderate"),
    HARD("Hard"),
    EXPERT("Expert"),
}

// Valeurs alignées sur le type enum Supabase `hike_status`
enum class HikeStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED,
}

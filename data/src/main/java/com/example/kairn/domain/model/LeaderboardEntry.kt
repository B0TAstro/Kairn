package com.example.kairn.domain.model

/**
 * Domain model for a single row in the leaderboard.
 *
 * [rank] is 1-based and computed client-side from the sorted list position.
 */
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val level: Int,
    val xp: Long,
    val isCurrentUser: Boolean,
) {
    companion object {
        val preview = LeaderboardEntry(
            rank = 1,
            userId = "123",
            username = "JohnHiker",
            avatarUrl = null,
            level = 5,
            xp = 1250,
            isCurrentUser = true,
        )

        val previewList = listOf(
            LeaderboardEntry(1, "u1", "AlpineMaster", null, 12, 5400, false),
            LeaderboardEntry(2, "u2", "TrailRunner", null, 10, 4200, false),
            LeaderboardEntry(3, "u3", "MountainGoat", null, 9, 3800, false),
            LeaderboardEntry(4, "u4", "HikePro", null, 8, 3100, false),
            LeaderboardEntry(5, "123", "JohnHiker", null, 5, 1250, true),
            LeaderboardEntry(6, "u6", "NatureLover", null, 4, 900, false),
            LeaderboardEntry(7, "u7", "PathFinder", null, 3, 650, false),
        )
    }
}

package com.example.kairn.domain.model

import kotlinx.datetime.Instant

data class Friendship(
    val id: String,
    val requesterId: String,
    val addresseeId: String,
    val status: FriendshipStatus,
    val friend: User,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    BLOCKED
}

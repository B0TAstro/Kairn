package com.example.kairn.domain.model

import kotlinx.datetime.Instant

/**
 * Represents a group chat
 */
data class Group(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String? = null,
    val visibility: GroupVisibility = GroupVisibility.PRIVATE,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/**
 * Represents a member of a group
 */
data class GroupMember(
    val groupId: String,
    val userId: String,
    val role: GroupRole,
    val user: User? = null,
    val joinedAt: Instant,
)

/**
 * Group member roles with different permissions
 */
enum class GroupRole {
    OWNER,  // Creator, all rights (delete group, change roles, etc.)
    ADMIN,  // Can add/remove members, manage settings
    MEMBER  // Standard member, can send messages
}

/**
 * Group visibility settings
 */
enum class GroupVisibility {
    PUBLIC,   // Anyone can find and join
    PRIVATE   // Invite-only
}

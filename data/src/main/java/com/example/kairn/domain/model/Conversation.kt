package com.example.kairn.domain.model

import kotlinx.datetime.Instant

data class Conversation(
    val id: String,
    val type: ConversationType,
    val groupId: String? = null,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0,
    val otherUser: User? = null, // For DIRECT conversations
    val groupName: String? = null, // For GROUP conversations
    val groupMembers: List<GroupMember>? = null, // For GROUP conversations
    val groupAvatar: String? = null, // For GROUP conversations
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val displayName: String
        get() = when (type) {
            ConversationType.DIRECT -> otherUser?.username ?: otherUser?.email ?: "Unknown"
            ConversationType.GROUP -> groupName ?: "Group Chat"
        }

    val avatarInitials: String
        get() = when (type) {
            ConversationType.DIRECT -> otherUser?.let {
                (it.username ?: it.email).take(2).uppercase()
            } ?: "?"
            ConversationType.GROUP -> groupName?.take(2)?.uppercase() ?: "G"
        }
}

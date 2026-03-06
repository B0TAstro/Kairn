package com.example.kairn.domain.model

import kotlinx.datetime.Instant

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderInitials: String,
    val body: String,
    val messageType: MessageType = MessageType.TEXT,
    val createdAt: Instant,
    val isCurrentUser: Boolean = false,
)

enum class MessageType {
    TEXT,
    IMAGE,
    SYSTEM
}

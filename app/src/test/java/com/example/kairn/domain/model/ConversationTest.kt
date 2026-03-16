package com.example.kairn.domain.model

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationTest {

    private val now = Instant.parse("2025-01-15T10:00:00Z")

    private val otherUser = User(
        id = "user-1",
        email = "alice@example.com",
        username = "AliceHiker",
    )

    private fun directConversation(otherUser: User? = this.otherUser) = Conversation(
        id = "conv-1",
        type = ConversationType.DIRECT,
        otherUser = otherUser,
        createdAt = now,
        updatedAt = now,
    )

    private fun groupConversation(groupName: String? = "Trail Runners") = Conversation(
        id = "conv-2",
        type = ConversationType.GROUP,
        groupName = groupName,
        createdAt = now,
        updatedAt = now,
    )

    // --- displayName ---

    @Test
    fun displayName_returnsUsername_forDirectConversation() {
        val conversation = directConversation()

        assertEquals("AliceHiker", conversation.displayName)
    }

    @Test
    fun displayName_returnsEmail_whenUsernameIsNull_forDirectConversation() {
        val user = otherUser.copy(username = null)
        val conversation = directConversation(otherUser = user)

        assertEquals("alice@example.com", conversation.displayName)
    }

    @Test
    fun displayName_returnsUnknown_whenOtherUserIsNull_forDirectConversation() {
        val conversation = directConversation(otherUser = null)

        assertEquals("Unknown", conversation.displayName)
    }

    @Test
    fun displayName_returnsGroupName_forGroupConversation() {
        val conversation = groupConversation()

        assertEquals("Trail Runners", conversation.displayName)
    }

    @Test
    fun displayName_returnsDefault_whenGroupNameIsNull() {
        val conversation = groupConversation(groupName = null)

        assertEquals("Group Chat", conversation.displayName)
    }

    // --- avatarInitials ---

    @Test
    fun avatarInitials_returnsFirstTwoCharsOfUsername_forDirectConversation() {
        val conversation = directConversation()

        assertEquals("AL", conversation.avatarInitials)
    }

    @Test
    fun avatarInitials_returnsFirstTwoCharsOfEmail_whenUsernameIsNull() {
        val user = otherUser.copy(username = null)
        val conversation = directConversation(otherUser = user)

        assertEquals("AL", conversation.avatarInitials)
    }

    @Test
    fun avatarInitials_returnsQuestionMark_whenOtherUserIsNull() {
        val conversation = directConversation(otherUser = null)

        assertEquals("?", conversation.avatarInitials)
    }

    @Test
    fun avatarInitials_returnsFirstTwoCharsOfGroupName_forGroupConversation() {
        val conversation = groupConversation()

        assertEquals("TR", conversation.avatarInitials)
    }

    @Test
    fun avatarInitials_returnsG_whenGroupNameIsNull() {
        val conversation = groupConversation(groupName = null)

        assertEquals("G", conversation.avatarInitials)
    }
}

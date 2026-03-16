package com.example.kairn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GroupModelTest {

    @Test
    fun groupRole_hasCorrectValues() {
        val roles = GroupRole.entries
        assertEquals(3, roles.size)
        assertEquals(GroupRole.OWNER, roles[0])
        assertEquals(GroupRole.ADMIN, roles[1])
        assertEquals(GroupRole.MEMBER, roles[2])
    }

    @Test
    fun groupVisibility_hasCorrectValues() {
        val visibilities = GroupVisibility.entries
        assertEquals(2, visibilities.size)
        assertEquals(GroupVisibility.PUBLIC, visibilities[0])
        assertEquals(GroupVisibility.PRIVATE, visibilities[1])
    }

    @Test
    fun conversationType_hasCorrectValues() {
        val types = ConversationType.entries
        assertEquals(2, types.size)
        assertEquals(ConversationType.DIRECT, types[0])
        assertEquals(ConversationType.GROUP, types[1])
    }

    @Test
    fun messageType_hasCorrectValues() {
        val types = MessageType.entries
        assertEquals(3, types.size)
        assertEquals(MessageType.TEXT, types[0])
        assertEquals(MessageType.IMAGE, types[1])
        assertEquals(MessageType.SYSTEM, types[2])
    }

    @Test
    fun friendshipStatus_hasCorrectValues() {
        val statuses = FriendshipStatus.entries
        assertEquals(4, statuses.size)
        assertEquals(FriendshipStatus.PENDING, statuses[0])
        assertEquals(FriendshipStatus.ACCEPTED, statuses[1])
        assertEquals(FriendshipStatus.DECLINED, statuses[2])
        assertEquals(FriendshipStatus.BLOCKED, statuses[3])
    }
}

package com.example.kairn.domain.repository

import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.User
import kotlinx.coroutines.flow.Flow

interface FriendshipRepository {
    /**
     * Get all friends (accepted friendships) as a reactive flow.
     * Call [refreshFriends] to update the data.
     */
    fun getFriends(): Flow<List<Friendship>>

    /**
     * Get pending friend requests (received) as a reactive flow.
     * Call [refreshPendingRequests] to update the data.
     */
    fun getPendingRequests(): Flow<List<Friendship>>

    /**
     * Refresh the friends list from the database.
     */
    suspend fun refreshFriends()

    /**
     * Refresh the pending requests list from the database.
     */
    suspend fun refreshPendingRequests()

    /**
     * Search users by username
     */
    suspend fun searchUsers(query: String): Result<List<User>>

    /**
     * Send a friend request to another user
     */
    suspend fun sendFriendRequest(userId: String): Result<Unit>

    /**
     * Accept a friend request
     */
    suspend fun acceptFriendRequest(friendshipId: String): Result<Unit>

    /**
     * Decline a friend request
     */
    suspend fun declineFriendRequest(friendshipId: String): Result<Unit>

    /**
     * Remove a friend (delete friendship)
     */
    suspend fun removeFriend(friendshipId: String): Result<Unit>
}

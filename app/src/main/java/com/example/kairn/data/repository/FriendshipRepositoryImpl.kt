package com.example.kairn.data.repository

import android.util.Log
import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.FriendshipStatus
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.FriendshipRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FriendshipRepo"

/**
 * Friendship Repository - simplified architecture (no Realtime).
 *
 * Uses MutableStateFlow + explicit refresh, matching the pattern in ChatRepositoryImpl.
 * This avoids crashes caused by Realtime channel reuse when re-entering screens.
 */
@Singleton
class FriendshipRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
) : FriendshipRepository {

    private val _friends = MutableStateFlow<List<Friendship>>(emptyList())
    private val _pendingRequests = MutableStateFlow<List<Friendship>>(emptyList())

    private val currentUserId: String?
        get() = auth.currentUserOrNull()?.id

    // ==================== FRIENDS ====================

    override fun getFriends(): Flow<List<Friendship>> = _friends

    override suspend fun refreshFriends() {
        val userId = currentUserId ?: run {
            Log.e(TAG, "refreshFriends: User not authenticated")
            return
        }

        try {
            Log.d(TAG, "refreshFriends: Fetching friends from database")
            val dtos = postgrest.from("friendships")
                .select(
                    Columns.raw(
                        """
                        *,
                        requester:requester_id(id, username),
                        addressee:addressee_id(id, username)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        or {
                            eq("requester_id", userId)
                            eq("addressee_id", userId)
                        }
                        eq("status", "ACCEPTED")
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<FriendshipDto>()

            _friends.value = dtos.map { it.toDomain(userId) }
            Log.d(TAG, "refreshFriends: SUCCESS - ${_friends.value.size} friends loaded")
        } catch (e: Exception) {
            Log.e(TAG, "refreshFriends: Error", e)
        }
    }

    // ==================== PENDING REQUESTS ====================

    override fun getPendingRequests(): Flow<List<Friendship>> = _pendingRequests

    override suspend fun refreshPendingRequests() {
        val userId = currentUserId ?: run {
            Log.e(TAG, "refreshPendingRequests: User not authenticated")
            return
        }

        try {
            Log.d(TAG, "refreshPendingRequests: Fetching pending requests from database")
            val dtos = postgrest.from("friendships")
                .select(
                    Columns.raw(
                        """
                        *,
                        requester:requester_id(id, username),
                        addressee:addressee_id(id, username)
                        """.trimIndent()
                    )
                ) {
                    filter {
                        eq("addressee_id", userId)
                        eq("status", "PENDING")
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<FriendshipDto>()

            _pendingRequests.value = dtos.map { it.toDomain(userId) }
            Log.d(TAG, "refreshPendingRequests: SUCCESS - ${_pendingRequests.value.size} pending requests loaded")
        } catch (e: Exception) {
            Log.e(TAG, "refreshPendingRequests: Error", e)
        }
    }

    // ==================== SEARCH ====================

    override suspend fun searchUsers(query: String): Result<List<User>> = runCatching {
        Log.d(TAG, "searchUsers: query='$query'")

        val userId = currentUserId
            ?: throw IllegalStateException("User not authenticated")

        val dtos = postgrest.from("profiles")
            .select() {
                filter {
                    neq("id", userId)
                    ilike("username", "%$query%")
                }
                limit(20)
            }
            .decodeList<UserDto>()

        Log.d(TAG, "searchUsers: SUCCESS - found ${dtos.size} users")
        dtos.map { it.toDomain() }
    }

    // ==================== MUTATIONS ====================

    override suspend fun sendFriendRequest(userId: String): Result<Unit> = runCatching {
        Log.d(TAG, "sendFriendRequest: userId=$userId")

        val myUserId = currentUserId
            ?: throw IllegalStateException("User not authenticated")

        val insertDto = FriendshipInsertDto(
            requesterId = myUserId,
            addresseeId = userId,
            status = "PENDING",
        )

        postgrest.from("friendships").insert(insertDto)
        Log.d(TAG, "sendFriendRequest: SUCCESS")

        // Refresh both lists after mutation
        refreshFriends()
        refreshPendingRequests()
    }

    override suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        Log.d(TAG, "acceptFriendRequest: friendshipId=$friendshipId")

        postgrest.from("friendships")
            .update({
                set("status", "ACCEPTED")
            }) {
                filter { eq("id", friendshipId) }
            }

        Log.d(TAG, "acceptFriendRequest: SUCCESS")

        // Refresh both lists after mutation
        refreshFriends()
        refreshPendingRequests()
    }

    override suspend fun declineFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        Log.d(TAG, "declineFriendRequest: friendshipId=$friendshipId")

        postgrest.from("friendships")
            .update({
                set("status", "DECLINED")
            }) {
                filter { eq("id", friendshipId) }
            }

        Log.d(TAG, "declineFriendRequest: SUCCESS")

        // Refresh pending requests after mutation
        refreshPendingRequests()
    }

    override suspend fun removeFriend(friendshipId: String): Result<Unit> = runCatching {
        Log.d(TAG, "removeFriend: friendshipId=$friendshipId")

        postgrest.from("friendships")
            .delete {
                filter { eq("id", friendshipId) }
            }

        Log.d(TAG, "removeFriend: SUCCESS")

        // Refresh friends list after mutation
        refreshFriends()
    }
}

// ==================== DTOs ====================

@Serializable
private data class FriendshipDto(
    val id: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("addressee_id") val addresseeId: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val requester: UserDto? = null,
    val addressee: UserDto? = null,
) {
    fun toDomain(currentUserId: String): Friendship {
        val isRequester = requesterId == currentUserId
        val friendUser = (if (isRequester) addressee else requester)
            ?: throw IllegalStateException("Friend user not found")

        return Friendship(
            id = id,
            requesterId = requesterId,
            addresseeId = addresseeId,
            status = FriendshipStatus.valueOf(status.uppercase()),
            friend = friendUser.toDomain(),
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
        )
    }
}

@Serializable
private data class FriendshipInsertDto(
    @SerialName("requester_id") val requesterId: String,
    @SerialName("addressee_id") val addresseeId: String,
    val status: String,
)

@Serializable
private data class UserDto(
    val id: String,
    val username: String? = null,
) {
    fun toDomain() = User(
        id = id,
        email = "", // Email not available from profiles table
        username = username,
        avatarUrl = null,
        level = 1,
        xp = 0,
        city = null,
        region = null,
        country = null,
    )
}

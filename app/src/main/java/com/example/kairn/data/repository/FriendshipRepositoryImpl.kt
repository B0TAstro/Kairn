package com.example.kairn.data.repository

import android.util.Log
import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.FriendshipStatus
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.FriendshipRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FriendshipRepo"

@Singleton
class FriendshipRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
    private val realtime: Realtime,
) : FriendshipRepository {

    override fun getFriends(): Flow<List<Friendship>> = callbackFlow {
        val currentUserId = auth.currentUserOrNull()?.id ?: run {
            send(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "getFriends: Starting realtime subscription for user $currentUserId")

        suspend fun fetchFriends() {
            Log.d(TAG, "getFriends: Fetching friends from database")
            val dtos = postgrest.from("friendships")
                .select(
                    Columns.raw("""
                        *,
                        requester:requester_id(id, username),
                        addressee:addressee_id(id, username)
                    """.trimIndent())
                ) {
                    filter {
                        or {
                            eq("requester_id", currentUserId)
                            eq("addressee_id", currentUserId)
                        }
                        eq("status", "ACCEPTED")
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<FriendshipDto>()

            val friends = dtos.map { it.toDomain(currentUserId) }
            Log.d(TAG, "getFriends: Sending ${friends.size} friends to flow")
            send(friends)
        }

        // Initial fetch
        fetchFriends()

        // Subscribe to realtime changes
        val channel = realtime.channel("friendships-${currentUserId}")
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "friendships"
        }.onEach { action ->
            Log.d(TAG, "getFriends: Realtime change detected: ${action.javaClass.simpleName}")
            fetchFriends()
        }.launchIn(this)

        channel.subscribe()

        awaitClose {
            Log.d(TAG, "getFriends: Closing realtime subscription")
            launch { channel.unsubscribe() }
        }
    }

    override fun getPendingRequests(): Flow<List<Friendship>> = callbackFlow {
        val currentUserId = auth.currentUserOrNull()?.id ?: run {
            send(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "getPendingRequests: Starting realtime subscription for user $currentUserId")

        suspend fun fetchPendingRequests() {
            Log.d(TAG, "getPendingRequests: Fetching pending requests from database")
            val dtos = postgrest.from("friendships")
                .select(
                    Columns.raw("""
                        *,
                        requester:requester_id(id, username),
                        addressee:addressee_id(id, username)
                    """.trimIndent())
                ) {
                    filter {
                        eq("addressee_id", currentUserId)
                        eq("status", "PENDING")
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<FriendshipDto>()

            val pending = dtos.map { it.toDomain(currentUserId) }
            Log.d(TAG, "getPendingRequests: Sending ${pending.size} pending requests to flow")
            send(pending)
        }

        // Initial fetch
        fetchPendingRequests()

        // Subscribe to realtime changes (reuse same channel as getFriends)
        val channel = realtime.channel("friendships-pending-${currentUserId}")
        channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "friendships"
        }.onEach { action ->
            Log.d(TAG, "getPendingRequests: Realtime change detected: ${action.javaClass.simpleName}")
            fetchPendingRequests()
        }.launchIn(this)

        channel.subscribe()

        awaitClose {
            Log.d(TAG, "getPendingRequests: Closing realtime subscription")
            launch { channel.unsubscribe() }
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> = runCatching {
        Log.d(TAG, "searchUsers: query='$query'")
        
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")
        
        Log.d(TAG, "searchUsers: currentUserId=$currentUserId")

        val dtos = postgrest.from("profiles")
            .select() {
                filter {
                    neq("id", currentUserId)
                    ilike("username", "%$query%")
                }
                limit(20)
            }
            .decodeList<UserDto>()

        Log.d(TAG, "searchUsers: received ${dtos.size} DTOs from Supabase")
        dtos.forEachIndexed { index, dto ->
            Log.d(TAG, "  [$index] DTO: id=${dto.id}, username=${dto.username}")
        }

        val users = dtos.map { it.toDomain() }
        Log.d(TAG, "searchUsers: returning ${users.size} users")
        users
    }

    override suspend fun sendFriendRequest(userId: String): Result<Unit> = runCatching {
        Log.d(TAG, "sendFriendRequest: userId=$userId")
        
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")
        
        Log.d(TAG, "sendFriendRequest: currentUserId=$currentUserId")

        val insertDto = FriendshipInsertDto(
            requesterId = currentUserId,
            addresseeId = userId,
            status = "PENDING"
        )
        
        Log.d(TAG, "sendFriendRequest: inserting friendship request")

        postgrest.from("friendships")
            .insert(insertDto)
        
        Log.d(TAG, "sendFriendRequest: SUCCESS - request sent")
    }

    override suspend fun acceptFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        postgrest.from("friendships")
            .update({
                set("status", "ACCEPTED")
            }) {
                filter {
                    eq("id", friendshipId)
                }
            }
    }

    override suspend fun declineFriendRequest(friendshipId: String): Result<Unit> = runCatching {
        postgrest.from("friendships")
            .update({
                set("status", "DECLINED")
            }) {
                filter {
                    eq("id", friendshipId)
                }
            }
    }

    override suspend fun removeFriend(friendshipId: String): Result<Unit> = runCatching {
        postgrest.from("friendships")
            .delete {
                filter {
                    eq("id", friendshipId)
                }
            }
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

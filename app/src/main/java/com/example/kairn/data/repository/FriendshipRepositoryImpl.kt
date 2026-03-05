package com.example.kairn.data.repository

import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.FriendshipStatus
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.FriendshipRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendshipRepositoryImpl @Inject constructor(
    private val auth: Auth,
    private val postgrest: Postgrest,
) : FriendshipRepository {

    override fun getFriends(): Flow<List<Friendship>> = flow {
        val currentUserId = auth.currentUserOrNull()?.id ?: run {
            emit(emptyList())
            return@flow
        }

        val dtos = postgrest.from("friendships")
            .select(
                Columns.raw("""
                    *,
                    requester:requester_id(id, username, email),
                    addressee:addressee_id(id, username, email)
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

        emit(dtos.map { it.toDomain(currentUserId) })
    }

    override fun getPendingRequests(): Flow<List<Friendship>> = flow {
        val currentUserId = auth.currentUserOrNull()?.id ?: run {
            emit(emptyList())
            return@flow
        }

        val dtos = postgrest.from("friendships")
            .select(
                Columns.raw("""
                    *,
                    requester:requester_id(id, username, email),
                    addressee:addressee_id(id, username, email)
                """.trimIndent())
            ) {
                filter {
                    eq("addressee_id", currentUserId)
                    eq("status", "PENDING")
                }
                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<FriendshipDto>()

        emit(dtos.map { it.toDomain(currentUserId) })
    }

    override suspend fun searchUsers(query: String): Result<List<User>> = runCatching {
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")

        val dtos = postgrest.from("profiles")
            .select() {
                filter {
                    neq("id", currentUserId)
                    or {
                        ilike("username", "%$query%")
                        ilike("email", "%$query%")
                    }
                }
                limit(20)
            }
            .decodeList<UserDto>()

        dtos.map { it.toDomain() }
    }

    override suspend fun sendFriendRequest(userId: String): Result<Unit> = runCatching {
        val currentUserId = auth.currentUserOrNull()?.id 
            ?: throw IllegalStateException("User not authenticated")

        val insertDto = FriendshipInsertDto(
            requesterId = currentUserId,
            addresseeId = userId,
            status = "PENDING"
        )

        postgrest.from("friendships")
            .insert(insertDto)
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
    val email: String,
) {
    fun toDomain() = User(
        id = id,
        email = email,
        username = username,
        avatarUrl = null,
        level = 1,
        xp = 0,
        city = null,
        region = null,
        country = null,
    )
}

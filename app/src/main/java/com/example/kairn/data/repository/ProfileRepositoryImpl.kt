package com.example.kairn.data.repository

import com.example.kairn.data.remote.ProfileDto
import com.example.kairn.data.remote.ProfileUpdateDto
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.ProfileRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val storage: Storage,
) : ProfileRepository {

    companion object {
        private const val PROFILES_TABLE = "profiles"
        private const val AVATARS_BUCKET = "avatars"
    }

    override suspend fun getProfile(userId: String, currentUser: User): Result<User> =
        runCatching {
            val dto = postgrest
                .from(PROFILES_TABLE)
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<ProfileDto>()

            if (dto != null) {
                currentUser.copy(
                    pseudo = dto.username ?: currentUser.pseudo,
                    username = dto.username ?: currentUser.username,
                    avatarUrl = dto.avatarUrl ?: currentUser.avatarUrl,
                    bio = dto.bio ?: currentUser.bio,
                    country = dto.countryCode ?: currentUser.country,
                    createdAt = dto.createdAt ?: currentUser.createdAt,
                )
            } else {
                currentUser
            }
        }

    override suspend fun updateProfile(
        userId: String,
        pseudo: String?,
        bio: String?,
        city: String?,
        avatarUrl: String?,
    ): Result<Unit> = runCatching {
        val update = ProfileUpdateDto(
            username = pseudo,
            bio = bio,
            avatarUrl = avatarUrl,
        )
        postgrest
            .from(PROFILES_TABLE)
            .update(update) {
                filter { eq("id", userId) }
            }
    }

    override suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String> =
        runCatching {
            val path = "$userId.jpg"
            val bucket = storage.from(AVATARS_BUCKET)

            // Upsert: upload will overwrite if file already exists
            bucket.upload(path, imageBytes) { upsert = true }

            // Return the public URL
            bucket.publicUrl(path)
        }
}

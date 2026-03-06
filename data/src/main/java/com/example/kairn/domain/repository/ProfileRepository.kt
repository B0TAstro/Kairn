package com.example.kairn.domain.repository

import com.example.kairn.domain.model.User

interface ProfileRepository {

    /**
     * Fetches the full profile from the `profiles` table for the given [userId].
     * Merges result with any auth metadata already available in [currentUser].
     */
    suspend fun getProfile(userId: String, currentUser: User): Result<User>

    /**
     * Updates the user's public profile fields in the `profiles` table.
     */
    suspend fun updateProfile(
        userId: String,
        pseudo: String?,
        bio: String?,
        city: String?,
        avatarUrl: String?,
    ): Result<Unit>

    /**
     * Uploads an avatar image to Supabase Storage (bucket: `avatars`).
     * Returns the public URL of the uploaded image.
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): Result<String>
}

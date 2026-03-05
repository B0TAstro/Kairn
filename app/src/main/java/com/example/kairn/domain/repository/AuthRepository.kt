package com.example.kairn.domain.repository

import com.example.kairn.domain.model.SessionState
import com.example.kairn.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    /** Reactive session state driven by the Supabase SDK's sessionStatus flow. */
    val sessionState: StateFlow<SessionState>

    /** Convenience: the currently authenticated user, or null. */
    val currentUser: StateFlow<User?>

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
}

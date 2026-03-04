package com.example.kairn.data.repository

import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.AuthRepository
import io.github.jan-tennert.supabase.gotrue.Auth
import io.github.jan-tennert.supabase.gotrue.gotrue
import io.github.jan-tennert.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Initialize current user from existing session
        val session = auth.currentSessionOrNull()
        if (session != null) {
            _currentUser.value = session.user?.toDomain()
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(email = email, password = password)
        val session = auth.currentSessionOrNull()
        _currentUser.value = session?.user?.toDomain()
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        auth.signUpWith(email = email, password = password)
        // After signup, user needs to verify email before signing in automatically
        // For immediate login, call signIn after successful signUp
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.logout()
        _currentUser.update { null }
    }

    override fun getCurrentUser(): StateFlow<User?> = currentUser

    override fun isAuthenticated(): Boolean = _currentUser.value != null

    private fun UserInfo.toDomain(): User = User(
        id = id,
        email = email ?: "",
        username = email?.substringBefore("@")
    )
}
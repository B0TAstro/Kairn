package com.example.kairn.data.repository

import com.example.kairn.domain.model.SessionState
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.AuthRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: Auth,
) : AuthRepository {

    /**
     * Long-lived scope that survives ViewModel clears. Tied to the singleton lifetime.
     * Uses [SupervisorJob] so a single collection failure doesn't cancel the scope.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    override val currentUser: StateFlow<User?> = _sessionState
        .map { state ->
            when (state) {
                is SessionState.Authenticated -> state.user
                else -> null
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    init {
        scope.launch {
            auth.sessionStatus.collect { status ->
                _sessionState.value = status.toDomain()
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        // Session state will be updated automatically via the sessionStatus flow
    }

    override suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        pseudo: String,
    ): Result<Unit> = runCatching {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("first_name", firstName)
                put("last_name", lastName)
                put("pseudo", pseudo)
            }
        }
        // If auto-confirm is disabled, user needs to verify email first.
        // The sessionStatus flow will remain NotAuthenticated until they do.
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
        // Session state will be updated automatically via the sessionStatus flow
    }

    // ---------------------------------------------------------------------------
    // Mapping helpers
    // ---------------------------------------------------------------------------

    private fun SessionStatus.toDomain(): SessionState = when (this) {
        is SessionStatus.Authenticated -> {
            val user = session.user?.toDomain()
            if (user != null) {
                SessionState.Authenticated(user)
            } else {
                // Edge case: session exists but no user info attached
                SessionState.NotAuthenticated()
            }
        }
        is SessionStatus.NotAuthenticated -> SessionState.NotAuthenticated(isSignOut = isSignOut)
        is SessionStatus.RefreshFailure -> SessionState.NotAuthenticated()
        SessionStatus.Initializing -> SessionState.Loading
    }

    private fun UserInfo.toDomain(): User {
        val meta = userMetadata
        return User(
            id = id,
            email = email ?: "",
            firstName = meta?.get("first_name")?.toString()?.removeSurrounding("\""),
            lastName = meta?.get("last_name")?.toString()?.removeSurrounding("\""),
            pseudo = meta?.get("pseudo")?.toString()?.removeSurrounding("\""),
            username = meta?.get("pseudo")?.toString()?.removeSurrounding("\"")
                ?: email?.substringBefore("@") ?: "",
        )
    }
}

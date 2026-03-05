package com.example.kairn.domain.model

/**
 * Domain-level representation of the authentication session state.
 * Maps from Supabase's [SessionStatus] without leaking SDK types into the domain layer.
 */
sealed interface SessionState {
    /** The SDK is restoring a persisted session from local storage. */
    data object Loading : SessionState

    /** The user holds a valid, authenticated session. */
    data class Authenticated(val user: User) : SessionState

    /** No active session — either never signed in, signed out, or refresh failed. */
    data class NotAuthenticated(val isSignOut: Boolean = false) : SessionState
}

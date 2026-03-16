package com.example.kairn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionStateTest {

    @Test
    fun loading_isSingletonInstance() {
        val a = SessionState.Loading
        val b = SessionState.Loading

        assertEquals(a, b)
    }

    @Test
    fun authenticated_containsUser() {
        val user = User.preview
        val state = SessionState.Authenticated(user)

        assertEquals(user, state.user)
    }

    @Test
    fun notAuthenticated_defaultsIsSignOutToFalse() {
        val state = SessionState.NotAuthenticated()

        assertFalse(state.isSignOut)
    }

    @Test
    fun notAuthenticated_canBeSignOut() {
        val state = SessionState.NotAuthenticated(isSignOut = true)

        assertTrue(state.isSignOut)
    }

    @Test
    fun sealedInterface_coversAllCases() {
        val states: List<SessionState> = listOf(
            SessionState.Loading,
            SessionState.Authenticated(User.preview),
            SessionState.NotAuthenticated(),
        )

        assertEquals(3, states.size)
    }
}

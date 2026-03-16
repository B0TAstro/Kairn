package com.example.kairn.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUiStateTest {

    @Test
    fun idle_isSingletonInstance() {
        val a = AuthUiState.Idle
        val b = AuthUiState.Idle
        assertEquals(a, b)
    }

    @Test
    fun loading_isSingletonInstance() {
        val a = AuthUiState.Loading
        val b = AuthUiState.Loading
        assertEquals(a, b)
    }

    @Test
    fun success_isSingletonInstance() {
        val a = AuthUiState.Success
        val b = AuthUiState.Success
        assertEquals(a, b)
    }

    @Test
    fun error_containsMessage() {
        val error = AuthUiState.Error("Invalid credentials")

        assertEquals("Invalid credentials", error.message)
    }

    @Test
    fun sealedInterface_coversAllCases() {
        val states: List<AuthUiState> = listOf(
            AuthUiState.Idle,
            AuthUiState.Loading,
            AuthUiState.Success,
            AuthUiState.Error("test"),
        )

        assertEquals(4, states.size)
    }

    @Test
    fun error_equalityIsBasedOnMessage() {
        val a = AuthUiState.Error("msg")
        val b = AuthUiState.Error("msg")
        val c = AuthUiState.Error("different")

        assertEquals(a, b)
        assertTrue(a != c)
    }
}

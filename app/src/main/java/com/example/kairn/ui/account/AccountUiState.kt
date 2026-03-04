package com.example.kairn.ui.account

import com.example.kairn.domain.model.User

sealed interface AccountUiState {
    data object Loading : AccountUiState
    data class LogIn(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
    ) : AccountUiState
    data class SignIn(
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
    ) : AccountUiState
    data class Profile(val user: User) : AccountUiState
    data class Error(val message: String) : AccountUiState
}
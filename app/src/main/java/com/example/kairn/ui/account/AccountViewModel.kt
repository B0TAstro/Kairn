package com.example.kairn.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        simulateInitialLoading()
    }

    private fun simulateInitialLoading() {
        viewModelScope.launch {
            delay(500)
            _uiState.value = AccountUiState.LogIn()
        }
    }

    fun onEmailChange(email: String) {
        when (val current = _uiState.value) {
            is AccountUiState.LogIn -> _uiState.value = current.copy(email = email)
            is AccountUiState.SignIn -> _uiState.value = current.copy(email = email)
            else -> {}
        }
    }

    fun onPasswordChange(password: String) {
        when (val current = _uiState.value) {
            is AccountUiState.LogIn -> _uiState.value = current.copy(password = password)
            is AccountUiState.SignIn -> _uiState.value = current.copy(password = password)
            else -> {}
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        when (val current = _uiState.value) {
            is AccountUiState.SignIn -> _uiState.value = current.copy(confirmPassword = confirmPassword)
            else -> {}
        }
    }

    fun onLogIn() {
        when (val current = _uiState.value) {
            is AccountUiState.LogIn -> {
                _uiState.value = current.copy(isLoading = true)
                viewModelScope.launch {
                    delay(1000)
                    _uiState.value = AccountUiState.Profile(User.preview)
                }
            }
            else -> {}
        }
    }

    fun onSignIn() {
        when (val current = _uiState.value) {
            is AccountUiState.SignIn -> {
                _uiState.value = current.copy(isLoading = true)
                viewModelScope.launch {
                    delay(1000)
                    _uiState.value = AccountUiState.Profile(User.preview)
                }
            }
            else -> {}
        }
    }

    fun onNavigateToSignIn() {
        _uiState.value = AccountUiState.SignIn()
    }

    fun onNavigateToLogIn() {
        _uiState.value = AccountUiState.LogIn()
    }

    fun onSignOut() {
        _uiState.value = AccountUiState.LogIn()
    }
}
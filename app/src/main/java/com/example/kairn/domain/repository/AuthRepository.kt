package com.example.kairn.domain.repository

import com.example.kairn.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    
    fun getCurrentUser(): StateFlow<User?>
    fun isAuthenticated(): Boolean
}
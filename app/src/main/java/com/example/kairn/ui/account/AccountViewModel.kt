package com.example.kairn.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    companion object {
        /** XP required per level — simple linear formula for now. */
        private const val XP_PER_LEVEL = 500

        /**
         * Returns the user's initials derived from [User.pseudo], falling back to
         * [User.firstName]/[User.lastName], and finally to [User.email].
         */
        fun getInitials(user: User): String {
            // 1. Try pseudo (first two letters)
            user.pseudo?.takeIf { it.isNotBlank() }?.let { pseudo ->
                return pseudo.take(2).uppercase()
            }
            // 2. Try first + last name initials
            val first = user.firstName?.firstOrNull()
            val last = user.lastName?.firstOrNull()
            if (first != null || last != null) {
                return buildString {
                    first?.let { append(it.uppercaseChar()) }
                    last?.let { append(it.uppercaseChar()) }
                }
            }
            // 3. Fallback to email first letter
            return user.email.take(1).uppercase()
        }

        /**
         * Returns a pair of (current XP within current level, XP needed for next level).
         * E.g. level 5 with 1250 XP total → (1250 - 5*500, 500) if using cumulative,
         * but we keep it simple: xpInLevel = xp % XP_PER_LEVEL, xpForNext = XP_PER_LEVEL.
         */
        fun xpProgress(user: User): Pair<Int, Int> {
            val xpInLevel = user.xp % XP_PER_LEVEL
            return xpInLevel to XP_PER_LEVEL
        }

        /**
         * Returns XP progress as a fraction between 0f and 1f.
         */
        fun xpFraction(user: User): Float {
            val (current, total) = xpProgress(user)
            return if (total > 0) current.toFloat() / total.toFloat() else 0f
        }
    }
}

package com.example.kairn.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.AuthRepository
import com.example.kairn.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// UI State for the Edit Profile screen
// ─────────────────────────────────────────────────────────────────────────────

data class EditProfileUiState(
    val pseudo: String = "",
    val bio: String = "",
    val city: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    // --- Edit Profile state ---
    private val _editState = MutableStateFlow(EditProfileUiState())
    val editState: StateFlow<EditProfileUiState> = _editState.asStateFlow()

    /**
     * Initialise the edit form fields from the current user data.
     * Called when navigating to the Edit Profile screen.
     */
    fun initEditForm(user: User) {
        _editState.value = EditProfileUiState(
            pseudo = user.pseudo.orEmpty(),
            bio = user.bio.orEmpty(),
            city = user.city.orEmpty(),
            avatarUrl = user.avatarUrl,
        )
    }

    fun onPseudoChange(value: String) {
        _editState.update { it.copy(pseudo = value) }
    }

    fun onBioChange(value: String) {
        _editState.update { it.copy(bio = value) }
    }

    fun onCityChange(value: String) {
        _editState.update { it.copy(city = value) }
    }

    /**
     * Uploads a new avatar image and updates the local state with the returned URL.
     */
    fun uploadAvatar(imageBytes: ByteArray) {
        val userId = currentUser.value?.id ?: return
        viewModelScope.launch {
            _editState.update { it.copy(isSaving = true, errorMessage = null) }
            profileRepository.uploadAvatar(userId, imageBytes)
                .onSuccess { url ->
                    _editState.update { it.copy(avatarUrl = url, isSaving = false) }
                }
                .onFailure { error ->
                    _editState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Erreur lors de l'upload",
                        )
                    }
                }
        }
    }

    /**
     * Saves the profile to Supabase then refreshes the auth session user.
     */
    fun saveProfile() {
        val userId = currentUser.value?.id ?: return
        val state = _editState.value

        viewModelScope.launch {
            _editState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }

            profileRepository.updateProfile(
                userId = userId,
                pseudo = state.pseudo.ifBlank { null },
                bio = state.bio.ifBlank { null },
                city = state.city.ifBlank { null },
                avatarUrl = state.avatarUrl,
            ).onSuccess {
                // Refresh the session user so AccountScreen picks up changes
                authRepository.refreshProfile()
                _editState.update { it.copy(isSaving = false, saveSuccess = true) }
            }.onFailure { error ->
                _editState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Erreur lors de la sauvegarde",
                    )
                }
            }
        }
    }

    fun clearSaveSuccess() {
        _editState.update { it.copy(saveSuccess = false) }
    }

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

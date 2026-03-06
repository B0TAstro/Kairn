package com.example.kairn.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.AuthRepository
import com.example.kairn.domain.repository.HikeRepository
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
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val hikeRepository: HikeRepository,
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    // --- Completed hikes ---
    private val _completedHikes = MutableStateFlow<List<Hike>>(emptyList())
    val completedHikes: StateFlow<List<Hike>> = _completedHikes.asStateFlow()

    // --- Edit Profile state ---
    private val _editState = MutableStateFlow(EditProfileUiState())
    val editState: StateFlow<EditProfileUiState> = _editState.asStateFlow()

    init {
        loadCompletedHikes()
    }

    private fun loadCompletedHikes() {
        viewModelScope.launch {
            val userId = currentUser.value?.id ?: return@launch
            hikeRepository.getCompletedHikes(userId)
                .onSuccess { hikes ->
                    _completedHikes.value = hikes
                    // Update longestTrailKm on the user if we have hikes
                    if (hikes.isNotEmpty()) {
                        val longestKm = hikes
                            .mapNotNull { it.distanceM }
                            .maxOrNull()
                            ?.let { it / 1000.0 }
                            ?: 0.0
                        authRepository.currentUser.value?.let { user ->
                            if (longestKm > user.longestTrailKm) {
                                // Update locally — this is a derived stat
                                // The user object from auth is immutable; we compute on the fly
                            }
                        }
                    }
                }
        }
    }

    /**
     * Computes the longest trail distance in km from the completed hikes list.
     * Falls back to the value stored in [User.longestTrailKm] if no hikes loaded.
     */
    fun longestTrailKm(): Double {
        val fromHikes = _completedHikes.value
            .mapNotNull { it.distanceM }
            .maxOrNull()
            ?.let { it / 1000.0 }
        return fromHikes ?: (currentUser.value?.longestTrailKm ?: 0.0)
    }

    /**
     * Initialise the edit form fields from the current user data.
     * Called when navigating to the Edit Profile screen.
     */
    fun initEditForm(user: User) {
        _editState.value = EditProfileUiState(
            pseudo = user.pseudo.orEmpty(),
            bio = user.bio.orEmpty(),
            avatarUrl = user.avatarUrl,
        )
    }

    fun onPseudoChange(value: String) {
        _editState.update { it.copy(pseudo = value) }
    }

    fun onBioChange(value: String) {
        _editState.update { it.copy(bio = value) }
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
                city = null,
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
         * Formats a "Member Since" string from an ISO date.
         * E.g. "2023-05-15T10:30:00Z" → "MAY 2023".
         */
        fun formatMemberSince(createdAt: String?): String {
            if (createdAt.isNullOrBlank()) return "—"
            return try {
                // Parse ISO date: extract month and year
                val parts = createdAt.split("-")
                if (parts.size < 2) return "—"
                val year = parts[0]
                val monthNum = parts[1].toIntOrNull() ?: return "—"
                val monthName = listOf(
                    "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC",
                ).getOrElse(monthNum - 1) { "—" }
                "$monthName $year"
            } catch (_: Exception) {
                "—"
            }
        }
    }
}

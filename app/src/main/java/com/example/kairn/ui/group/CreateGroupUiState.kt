package com.example.kairn.ui.group

import com.example.kairn.domain.model.Friendship
import com.example.kairn.domain.model.User

/**
 * UI state for the Create Group screen
 */
data class CreateGroupUiState(
    val groupName: String = "",
    val availableFriendships: List<Friendship> = emptyList(),
    val selectedMemberIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreating: Boolean = false,
) {
    val isValid: Boolean
        get() = groupName.isNotBlank() && selectedMemberIds.size >= 2

    val availableFriends: List<User>
        get() = availableFriendships.map { it.friend }

    val selectedMembers: List<User>
        get() = availableFriends.filter { it.id in selectedMemberIds }
}

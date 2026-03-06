package com.example.kairn.ui.group

import com.example.kairn.domain.model.Group
import com.example.kairn.domain.model.GroupMember
import com.example.kairn.domain.model.GroupRole

/**
 * UI state for the Group Info screen
 */
data class GroupInfoUiState(
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val currentUserRole: GroupRole? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
) {
    val canManageMembers: Boolean
        get() = currentUserRole == GroupRole.OWNER || currentUserRole == GroupRole.ADMIN

    val canDelete: Boolean
        get() = currentUserRole == GroupRole.OWNER

    val isOwner: Boolean
        get() = currentUserRole == GroupRole.OWNER
}

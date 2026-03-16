package com.example.kairn.ui.group

import com.example.kairn.domain.model.GroupRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupInfoUiStateTest {

    // --- canManageMembers ---

    @Test
    fun canManageMembers_returnsTrue_whenOwner() {
        val state = GroupInfoUiState(currentUserRole = GroupRole.OWNER)

        assertTrue(state.canManageMembers)
    }

    @Test
    fun canManageMembers_returnsTrue_whenAdmin() {
        val state = GroupInfoUiState(currentUserRole = GroupRole.ADMIN)

        assertTrue(state.canManageMembers)
    }

    @Test
    fun canManageMembers_returnsFalse_whenMember() {
        val state = GroupInfoUiState(currentUserRole = GroupRole.MEMBER)

        assertFalse(state.canManageMembers)
    }

    @Test
    fun canManageMembers_returnsFalse_whenRoleIsNull() {
        val state = GroupInfoUiState(currentUserRole = null)

        assertFalse(state.canManageMembers)
    }

    // --- canDelete ---

    @Test
    fun canDelete_returnsTrue_onlyWhenOwner() {
        assertTrue(GroupInfoUiState(currentUserRole = GroupRole.OWNER).canDelete)
        assertFalse(GroupInfoUiState(currentUserRole = GroupRole.ADMIN).canDelete)
        assertFalse(GroupInfoUiState(currentUserRole = GroupRole.MEMBER).canDelete)
        assertFalse(GroupInfoUiState(currentUserRole = null).canDelete)
    }

    // --- isOwner ---

    @Test
    fun isOwner_returnsTrue_onlyWhenOwner() {
        assertTrue(GroupInfoUiState(currentUserRole = GroupRole.OWNER).isOwner)
        assertFalse(GroupInfoUiState(currentUserRole = GroupRole.ADMIN).isOwner)
        assertFalse(GroupInfoUiState(currentUserRole = GroupRole.MEMBER).isOwner)
        assertFalse(GroupInfoUiState(currentUserRole = null).isOwner)
    }

    // --- defaults ---

    @Test
    fun defaultState_hasExpectedDefaults() {
        val state = GroupInfoUiState()

        assertFalse(state.isLoading)
        assertFalse(state.isUpdating)
        assertFalse(state.canManageMembers)
        assertFalse(state.canDelete)
        assertFalse(state.isOwner)
        assertTrue(state.members.isEmpty())
    }
}

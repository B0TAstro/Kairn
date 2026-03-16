package com.example.kairn.ui.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class EditProfileUiStateTest {

    @Test
    fun defaults_areCorrect() {
        val state = EditProfileUiState()

        assertEquals("", state.pseudo)
        assertEquals("", state.bio)
        assertNull(state.avatarUrl)
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun copy_updatesFields() {
        val state = EditProfileUiState()
        val updated = state.copy(
            pseudo = "NewPseudo",
            bio = "New bio",
            avatarUrl = "https://example.com/avatar.jpg",
        )

        assertEquals("NewPseudo", updated.pseudo)
        assertEquals("New bio", updated.bio)
        assertEquals("https://example.com/avatar.jpg", updated.avatarUrl)
    }

    @Test
    fun savingState_isTracked() {
        val saving = EditProfileUiState(isSaving = true)
        val saved = saving.copy(isSaving = false, saveSuccess = true)

        assertFalse(saved.isSaving)
        assertEquals(true, saved.saveSuccess)
    }
}

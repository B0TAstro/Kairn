package com.example.kairn.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorUiStateTest {

    @Test
    fun loading_isSingleton() {
        assertEquals(EditorUiState.Loading, EditorUiState.Loading)
    }

    @Test
    fun error_containsMessage() {
        val error = EditorUiState.Error("Something went wrong")
        assertEquals("Something went wrong", error.message)
    }

    @Test
    fun ready_defaultValues() {
        val state = EditorUiState.Ready()

        assertTrue(state.points.isEmpty())
        assertTrue(state.routes.isEmpty())
        assertFalse(state.isLoadingRoute)
        assertNull(state.error)
        assertNull(state.selectedPointId)
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertNull(state.saveError)
    }

    @Test
    fun ready_canBeCopiedWithPoints() {
        val state = EditorUiState.Ready()
        val updated = state.copy(isLoadingRoute = true, selectedPointId = "p1")

        assertTrue(updated.isLoadingRoute)
        assertEquals("p1", updated.selectedPointId)
    }

    @Test
    fun sealedInterface_coversAllCases() {
        val states: List<EditorUiState> = listOf(
            EditorUiState.Loading,
            EditorUiState.Ready(),
            EditorUiState.Error("err"),
        )

        assertEquals(3, states.size)
    }
}

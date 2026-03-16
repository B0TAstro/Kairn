package com.example.kairn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserTest {

    @Test
    fun preview_hasExpectedValues() {
        val user = User.preview

        assertEquals("123", user.id)
        assertEquals("john@example.com", user.email)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals("JohnHiker", user.pseudo)
        assertEquals(5, user.level)
        assertEquals(1250, user.xp)
        assertEquals(23, user.hikesCompleted)
        assertEquals(142_000L, user.totalDistanceM)
        assertEquals(21.4, user.longestTrailKm, 0.01)
    }

    @Test
    fun defaultValues_areCorrect() {
        val user = User(id = "1", email = "test@test.com")

        assertNull(user.firstName)
        assertNull(user.lastName)
        assertNull(user.pseudo)
        assertNull(user.username)
        assertNull(user.avatarUrl)
        assertNull(user.bio)
        assertEquals(1, user.level)
        assertEquals(0, user.xp)
        assertEquals(0, user.hikesCompleted)
        assertEquals(0L, user.totalDistanceM)
        assertEquals(0.0, user.longestTrailKm, 0.01)
        assertNull(user.createdAt)
        assertNull(user.city)
        assertNull(user.region)
        assertNull(user.regionId)
        assertNull(user.country)
        assertNull(user.countryCode)
    }

    @Test
    fun copy_updatesSpecifiedFields() {
        val user = User.preview.copy(
            pseudo = "NewPseudo",
            level = 10,
            xp = 5000,
        )

        assertEquals("NewPseudo", user.pseudo)
        assertEquals(10, user.level)
        assertEquals(5000, user.xp)
        // Unchanged fields remain
        assertEquals("john@example.com", user.email)
        assertEquals("John", user.firstName)
    }
}

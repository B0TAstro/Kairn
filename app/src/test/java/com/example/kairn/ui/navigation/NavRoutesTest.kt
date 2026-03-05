package com.example.kairn.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class NavRoutesTest {

    @Test
    fun hikeDetail_buildsConcreteRoute_withGivenHikeId() {
        val route = NavRoutes.hikeDetail("abc-123")

        assertEquals("hike_detail/abc-123", route)
    }
}

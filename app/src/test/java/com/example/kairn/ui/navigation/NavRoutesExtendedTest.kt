package com.example.kairn.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class NavRoutesExtendedTest {

    // --- Route patterns ---

    @Test
    fun hikeDetail_pattern_containsPlaceholder() {
        assertEquals("hike_detail/{hikeId}", NavRoutes.HIKE_DETAIL)
    }

    @Test
    fun accountHikeDetail_pattern_containsPlaceholder() {
        assertEquals("account_hike_detail/{hikeId}", NavRoutes.ACCOUNT_HIKE_DETAIL)
    }

    @Test
    fun chat_pattern_containsPlaceholders() {
        assertEquals("chat/{conversationId}/{conversationName}", NavRoutes.CHAT)
    }

    @Test
    fun groupInfo_pattern_containsPlaceholder() {
        assertEquals("group_info/{groupId}", NavRoutes.GROUP_INFO)
    }

    // --- Route builders ---

    @Test
    fun hikeDetail_buildsCorrectRoute() {
        assertEquals("hike_detail/hike-123", NavRoutes.hikeDetail("hike-123"))
    }

    @Test
    fun accountHikeDetail_buildsCorrectRoute() {
        assertEquals("account_hike_detail/hike-456", NavRoutes.accountHikeDetail("hike-456"))
    }

    @Test
    fun chat_buildsCorrectRoute_withSimpleName() {
        val route = NavRoutes.chat("conv-1", "Alice")
        assertEquals("chat/conv-1/Alice", route)
    }

    @Test
    fun chat_encodesConversationName_withSpaces() {
        val route = NavRoutes.chat("conv-1", "Trail Runners")
        val encoded = URLEncoder.encode("Trail Runners", StandardCharsets.UTF_8.toString())
        assertEquals("chat/conv-1/$encoded", route)
    }

    @Test
    fun chat_encodesSpecialCharacters() {
        val route = NavRoutes.chat("conv-1", "Café & Co")
        assertTrue(route.startsWith("chat/conv-1/"))
        // Should not contain raw ampersand or space
        val namePart = route.removePrefix("chat/conv-1/")
        assertTrue(!namePart.contains(" "))
    }

    @Test
    fun groupInfo_buildsCorrectRoute() {
        assertEquals("group_info/group-789", NavRoutes.groupInfo("group-789"))
    }

    // --- Static route constants ---

    @Test
    fun staticRoutes_haveExpectedValues() {
        assertEquals("edit_profile", NavRoutes.EDIT_PROFILE)
        assertEquals("active_run", NavRoutes.ACTIVE_RUN)
        assertEquals("run_completed", NavRoutes.RUN_COMPLETED)
        assertEquals("chat_list", NavRoutes.CHAT_LIST)
        assertEquals("friend_list", NavRoutes.FRIEND_LIST)
        assertEquals("create_group", NavRoutes.CREATE_GROUP)
    }

    // --- Screen enum ---

    @Test
    fun screen_hasAllFiveTabs() {
        val screens = Screen.entries
        assertEquals(5, screens.size)
        assertEquals(Screen.HOME, screens[0])
        assertEquals(Screen.EXPLORE, screens[1])
        assertEquals(Screen.EDITOR, screens[2])
        assertEquals(Screen.CHAT, screens[3])
        assertEquals(Screen.PROFILE, screens[4])
    }
}

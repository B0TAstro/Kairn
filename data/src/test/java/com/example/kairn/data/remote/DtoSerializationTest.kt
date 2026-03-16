package com.example.kairn.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // --- ProfileDto ---

    @Test
    fun profileDto_deserializesFromJson() {
        val jsonString = """
            {
                "id": "user-1",
                "username": "JohnHiker",
                "avatar_url": "https://example.com/avatar.jpg",
                "bio": "Love hiking",
                "region_id": 42,
                "country_code": "FR",
                "created_at": "2023-05-15T10:30:00Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProfileDto>(jsonString)

        assertEquals("user-1", dto.id)
        assertEquals("JohnHiker", dto.username)
        assertEquals("https://example.com/avatar.jpg", dto.avatarUrl)
        assertEquals("Love hiking", dto.bio)
        assertEquals(42L, dto.regionId)
        assertEquals("FR", dto.countryCode)
        assertEquals("2023-05-15T10:30:00Z", dto.createdAt)
    }

    @Test
    fun profileDto_handlesNullOptionalFields() {
        val jsonString = """{"id": "user-1"}"""

        val dto = json.decodeFromString<ProfileDto>(jsonString)

        assertEquals("user-1", dto.id)
        assertNull(dto.username)
        assertNull(dto.avatarUrl)
        assertNull(dto.bio)
        assertNull(dto.regionId)
        assertNull(dto.countryCode)
        assertNull(dto.createdAt)
    }

    // --- ProfileUpdateDto ---

    @Test
    fun profileUpdateDto_serializesToJson() {
        val dto = ProfileUpdateDto(
            username = "NewName",
            bio = "New bio",
        )

        val serialized = json.encodeToString(ProfileUpdateDto.serializer(), dto)

        assertEquals(true, serialized.contains("\"username\":\"NewName\""))
        assertEquals(true, serialized.contains("\"bio\":\"New bio\""))
    }

    // --- ProfileGeoUpdateDto ---

    @Test
    fun profileGeoUpdateDto_serializesCorrectly() {
        val dto = ProfileGeoUpdateDto(
            countryCode = "FR",
            regionId = 42,
        )

        val serialized = json.encodeToString(ProfileGeoUpdateDto.serializer(), dto)

        assertEquals(true, serialized.contains("\"country_code\":\"FR\""))
        assertEquals(true, serialized.contains("\"region_id\":42"))
    }

    // --- RegionDto ---

    @Test
    fun regionDto_deserializesCorrectly() {
        val jsonString = """{"id": 1, "country_code": "FR", "name": "Île-de-France"}"""

        val dto = json.decodeFromString<RegionDto>(jsonString)

        assertEquals(1L, dto.id)
        assertEquals("FR", dto.countryCode)
        assertEquals("Île-de-France", dto.name)
    }

    // --- LeaderboardEntryDto ---

    @Test
    fun leaderboardEntryDto_deserializesCorrectly() {
        val jsonString = """
            {
                "user_id": "u1",
                "username": "AlpineMaster",
                "avatar_url": null,
                "level": 12,
                "total_xp": 5400,
                "region_id": 42,
                "country_code": "FR"
            }
        """.trimIndent()

        val dto = json.decodeFromString<LeaderboardEntryDto>(jsonString)

        assertEquals("u1", dto.userId)
        assertEquals("AlpineMaster", dto.username)
        assertNull(dto.avatarUrl)
        assertEquals(12, dto.level)
        assertEquals(5400L, dto.totalXp)
        assertEquals(42L, dto.regionId)
        assertEquals("FR", dto.countryCode)
    }

    @Test
    fun leaderboardEntryDto_defaultValues() {
        val jsonString = """{"user_id": "u1"}"""

        val dto = json.decodeFromString<LeaderboardEntryDto>(jsonString)

        assertEquals("u1", dto.userId)
        assertNull(dto.username)
        assertNull(dto.avatarUrl)
        assertEquals(1, dto.level)
        assertEquals(0L, dto.totalXp)
        assertNull(dto.regionId)
        assertNull(dto.countryCode)
    }

    // --- UserStatsDto ---

    @Test
    fun userStatsDto_deserializesCorrectly() {
        val jsonString = """
            {
                "user_id": "u1",
                "level": 5,
                "total_xp": 1250,
                "total_distance_m": 142000,
                "completed_runs_count": 23
            }
        """.trimIndent()

        val dto = json.decodeFromString<UserStatsDto>(jsonString)

        assertEquals("u1", dto.userId)
        assertEquals(5, dto.level)
        assertEquals(1250L, dto.totalXp)
        assertEquals(142000L, dto.totalDistanceM)
        assertEquals(23, dto.completedRunsCount)
    }

    @Test
    fun userStatsDto_defaultValues() {
        val jsonString = """{"user_id": "u1"}"""

        val dto = json.decodeFromString<UserStatsDto>(jsonString)

        assertEquals(1, dto.level)
        assertEquals(0L, dto.totalXp)
        assertEquals(0L, dto.totalDistanceM)
        assertEquals(0, dto.completedRunsCount)
    }

    // --- HikeDto serialization ---

    @Test
    fun hikeDto_deserializesFromJson() {
        val jsonString = """
            {
                "id": "hike-1",
                "creator_id": "creator-1",
                "title": "Mountain Trail",
                "description": "A great trail",
                "difficulty": "hard",
                "estimated_duration_min": 180,
                "distance_m": 12000,
                "elevation_gain_m": 800,
                "recommended_level": 3,
                "status": "published",
                "created_at": "2025-01-01T00:00:00Z",
                "updated_at": "2025-01-15T00:00:00Z",
                "location": "Alps",
                "image_url": "https://example.com/img.jpg",
                "category": "mountain"
            }
        """.trimIndent()

        val dto = json.decodeFromString<HikeDto>(jsonString)

        assertEquals("hike-1", dto.id)
        assertEquals("creator-1", dto.creatorId)
        assertEquals("Mountain Trail", dto.title)
        assertEquals("hard", dto.difficulty)
        assertEquals(180, dto.estimatedDurationMin)
        assertEquals(12000, dto.distanceM)
        assertEquals(800, dto.elevationGainM)
        assertEquals("published", dto.status)
        assertEquals("mountain", dto.category)
    }

    @Test
    fun hikeDto_handlesMinimalJson() {
        val jsonString = """{"id": "h1", "title": "Minimal"}"""

        val dto = json.decodeFromString<HikeDto>(jsonString)

        assertEquals("h1", dto.id)
        assertEquals("Minimal", dto.title)
        assertNull(dto.creatorId)
        assertNull(dto.description)
        assertEquals("moderate", dto.difficulty)
        assertEquals("draft", dto.status)
        assertNull(dto.category)
    }
}

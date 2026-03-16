package com.example.kairn.data.remote

import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.domain.model.HikeDifficulty
import com.example.kairn.domain.model.HikeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HikeDtoTest {

    private fun baseDto(
        difficulty: String = "moderate",
        status: String = "draft",
        category: String? = null,
        creatorId: String? = "creator-1",
    ) = HikeDto(
        id = "hike-1",
        creatorId = creatorId,
        title = "Test Hike",
        description = "A test hike",
        difficulty = difficulty,
        estimatedDurationMin = 120,
        distanceM = 5000,
        elevationGainM = 300,
        recommendedLevel = 2,
        status = status,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z",
        location = "Test Location",
        imageUrl = "https://example.com/img.jpg",
        category = category,
    )

    // --- difficulty mapping ---

    @Test
    fun toDomain_mapsEasyDifficulty() {
        val hike = baseDto(difficulty = "easy").toDomain()
        assertEquals(HikeDifficulty.EASY, hike.difficulty)
    }

    @Test
    fun toDomain_mapsModerateDifficulty() {
        val hike = baseDto(difficulty = "moderate").toDomain()
        assertEquals(HikeDifficulty.MODERATE, hike.difficulty)
    }

    @Test
    fun toDomain_mapsHardDifficulty() {
        val hike = baseDto(difficulty = "hard").toDomain()
        assertEquals(HikeDifficulty.HARD, hike.difficulty)
    }

    @Test
    fun toDomain_mapsExpertDifficulty() {
        val hike = baseDto(difficulty = "expert").toDomain()
        assertEquals(HikeDifficulty.EXPERT, hike.difficulty)
    }

    @Test
    fun toDomain_defaultsToModerate_forUnknownDifficulty() {
        val hike = baseDto(difficulty = "unknown").toDomain()
        assertEquals(HikeDifficulty.MODERATE, hike.difficulty)
    }

    @Test
    fun toDomain_difficultyIsCaseInsensitive() {
        assertEquals(HikeDifficulty.EASY, baseDto(difficulty = "EASY").toDomain().difficulty)
        assertEquals(HikeDifficulty.HARD, baseDto(difficulty = "Hard").toDomain().difficulty)
        assertEquals(HikeDifficulty.EXPERT, baseDto(difficulty = "EXPERT").toDomain().difficulty)
    }

    // --- status mapping ---

    @Test
    fun toDomain_mapsPublishedStatus() {
        val hike = baseDto(status = "published").toDomain()
        assertEquals(HikeStatus.PUBLISHED, hike.status)
    }

    @Test
    fun toDomain_mapsArchivedStatus() {
        val hike = baseDto(status = "archived").toDomain()
        assertEquals(HikeStatus.ARCHIVED, hike.status)
    }

    @Test
    fun toDomain_mapsDraftStatus() {
        val hike = baseDto(status = "draft").toDomain()
        assertEquals(HikeStatus.DRAFT, hike.status)
    }

    @Test
    fun toDomain_defaultsToDraft_forUnknownStatus() {
        val hike = baseDto(status = "unknown").toDomain()
        assertEquals(HikeStatus.DRAFT, hike.status)
    }

    // --- category mapping ---

    @Test
    fun toDomain_mapsMountainCategory() {
        val hike = baseDto(category = "mountain").toDomain()
        assertEquals(HikeCategory.MOUNTAIN, hike.category)
    }

    @Test
    fun toDomain_mapsForestCategory() {
        val hike = baseDto(category = "forest").toDomain()
        assertEquals(HikeCategory.FOREST, hike.category)
    }

    @Test
    fun toDomain_mapsLakeCategory() {
        val hike = baseDto(category = "lake").toDomain()
        assertEquals(HikeCategory.LAKE, hike.category)
    }

    @Test
    fun toDomain_mapsCaveCategory() {
        val hike = baseDto(category = "cave").toDomain()
        assertEquals(HikeCategory.CAVE, hike.category)
    }

    @Test
    fun toDomain_returnsNullCategory_forUnknownValue() {
        val hike = baseDto(category = "desert").toDomain()
        assertNull(hike.category)
    }

    @Test
    fun toDomain_returnsNullCategory_whenNull() {
        val hike = baseDto(category = null).toDomain()
        assertNull(hike.category)
    }

    @Test
    fun toDomain_categoryIsCaseInsensitive() {
        assertEquals(HikeCategory.MOUNTAIN, baseDto(category = "MOUNTAIN").toDomain().category)
        assertEquals(HikeCategory.FOREST, baseDto(category = "Forest").toDomain().category)
    }

    // --- field mapping ---

    @Test
    fun toDomain_mapsAllFields() {
        val hike = baseDto().toDomain()

        assertEquals("hike-1", hike.id)
        assertEquals("creator-1", hike.creatorId)
        assertEquals("Test Hike", hike.title)
        assertEquals("A test hike", hike.description)
        assertEquals(120, hike.estimatedDurationMin)
        assertEquals(5000, hike.distanceM)
        assertEquals(300, hike.elevationGainM)
        assertEquals(2, hike.recommendedLevel)
        assertEquals("2025-01-01T00:00:00Z", hike.createdAt)
        assertEquals("Test Location", hike.location)
        assertEquals("https://example.com/img.jpg", hike.imageUrl)
    }

    @Test
    fun toDomain_usesEmptyString_whenCreatorIdIsNull() {
        val hike = baseDto(creatorId = null).toDomain()
        assertEquals("", hike.creatorId)
    }

    @Test
    fun toDomain_preservesNullDescription() {
        val dto = baseDto().copy(description = null)
        assertNull(dto.toDomain().description)
    }

    @Test
    fun toDomain_preservesNullOptionalFields() {
        val dto = baseDto().copy(
            estimatedDurationMin = null,
            distanceM = null,
            elevationGainM = null,
            location = null,
            imageUrl = null,
        )
        val hike = dto.toDomain()

        assertNull(hike.estimatedDurationMin)
        assertNull(hike.distanceM)
        assertNull(hike.elevationGainM)
        assertNull(hike.location)
        assertNull(hike.imageUrl)
    }
}

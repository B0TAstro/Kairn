package com.example.kairn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HikeExtendedTest {

    @Test
    fun formattedDuration_returnsOnlyHours_whenMinutesAreZero() {
        val hike = Hike.preview.copy(estimatedDurationMin = 120)

        assertEquals("2h", hike.formattedDuration)
    }

    @Test
    fun formattedDuration_returnsOnlyMinutes_whenLessThan60() {
        val hike = Hike.preview.copy(estimatedDurationMin = 45)

        assertEquals("45min", hike.formattedDuration)
    }

    @Test
    fun formattedDistance_returnsDash_whenDistanceIsNull() {
        val hike = Hike.preview.copy(distanceM = null)

        assertEquals("—", hike.formattedDistance)
    }

    @Test
    fun formattedDistance_formatsDecimalKilometers() {
        val hike = Hike.preview.copy(distanceM = 12500)

        assertEquals("12.5km", hike.formattedDistance)
    }

    @Test
    fun formattedDistance_formatsExactKilometers() {
        val hike = Hike.preview.copy(distanceM = 1000)

        assertEquals("1.0km", hike.formattedDistance)
    }

    @Test
    fun formattedElevation_returnsValue_whenElevationIsPresent() {
        val hike = Hike.preview.copy(elevationGainM = 500)

        assertEquals("500m", hike.formattedElevation)
    }

    @Test
    fun hikeDifficulty_hasCorrectLabels() {
        assertEquals("Easy", HikeDifficulty.EASY.label)
        assertEquals("Moderate", HikeDifficulty.MODERATE.label)
        assertEquals("Hard", HikeDifficulty.HARD.label)
        assertEquals("Expert", HikeDifficulty.EXPERT.label)
    }

    @Test
    fun hikeCategory_hasCorrectLabels() {
        assertEquals("Mountain", HikeCategory.MOUNTAIN.label)
        assertEquals("Forest", HikeCategory.FOREST.label)
        assertEquals("Lake", HikeCategory.LAKE.label)
        assertEquals("Cave", HikeCategory.CAVE.label)
    }

    @Test
    fun hikeStatus_hasAllValues() {
        val statuses = HikeStatus.entries
        assertEquals(3, statuses.size)
        assertEquals(HikeStatus.DRAFT, statuses[0])
        assertEquals(HikeStatus.PUBLISHED, statuses[1])
        assertEquals(HikeStatus.ARCHIVED, statuses[2])
    }

    @Test
    fun preview_hasExpectedValues() {
        val preview = Hike.preview
        assertEquals("Aiguille du Midi", preview.title)
        assertEquals(HikeDifficulty.EXPERT, preview.difficulty)
        assertEquals(HikeStatus.PUBLISHED, preview.status)
        assertEquals(HikeCategory.MOUNTAIN, preview.category)
        assertEquals(630, preview.estimatedDurationMin)
        assertEquals(28000, preview.distanceM)
        assertEquals(3842, preview.elevationGainM)
    }

    @Test
    fun previewList_containsSixHikes() {
        assertEquals(6, Hike.previewList.size)
    }

    @Test
    fun previewList_containsAllCategories() {
        val categories = Hike.previewList.mapNotNull { it.category }.toSet()
        assertEquals(setOf(HikeCategory.MOUNTAIN, HikeCategory.LAKE, HikeCategory.FOREST), categories)
    }
}

package com.example.kairn.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HikeTest {

    @Test
    fun formattedDuration_returnsDash_whenDurationIsNull() {
        val hike = Hike.preview.copy(estimatedDurationMin = null)

        assertEquals("—", hike.formattedDuration)
    }

    @Test
    fun formattedDuration_formatsHoursAndMinutes_whenDurationHasBoth() {
        val hike = Hike.preview.copy(estimatedDurationMin = 125)

        assertEquals("2h 5min", hike.formattedDuration)
    }

    @Test
    fun formattedDistance_formatsKilometers_whenDistanceIsAtLeast1000m() {
        val hike = Hike.preview.copy(distanceM = 28000)

        assertEquals("28.0km", hike.formattedDistance)
    }

    @Test
    fun formattedDistance_formatsMeters_whenDistanceIsLessThan1000m() {
        val hike = Hike.preview.copy(distanceM = 850)

        assertEquals("850m", hike.formattedDistance)
    }

    @Test
    fun formattedElevation_returnsDash_whenElevationIsNull() {
        val hike = Hike.preview.copy(elevationGainM = null)

        assertEquals("—", hike.formattedElevation)
    }
}

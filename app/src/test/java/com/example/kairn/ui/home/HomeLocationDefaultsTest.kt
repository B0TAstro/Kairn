package com.example.kairn.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLocationDefaultsTest {

    @Test
    fun annecyCoordinates_areCorrect() {
        assertEquals(45.907972, ANNECY_AUSSEDAT_LATITUDE, 0.000001)
        assertEquals(6.103278, ANNECY_AUSSEDAT_LONGITUDE, 0.000001)
    }

    @Test
    fun annecyLabel_isNotEmpty() {
        assertEquals("3 Esplanade Auguste Aussedat, Annecy", ANNECY_AUSSEDAT_LABEL)
    }

    @Test
    fun defaultHomeCity_hasAnnecyValues() {
        assertEquals("Annecy", DEFAULT_HOME_CITY.name)
        assertEquals(ANNECY_AUSSEDAT_LATITUDE, DEFAULT_HOME_CITY.latitude, 0.000001)
        assertEquals(ANNECY_AUSSEDAT_LONGITUDE, DEFAULT_HOME_CITY.longitude, 0.000001)
    }
}

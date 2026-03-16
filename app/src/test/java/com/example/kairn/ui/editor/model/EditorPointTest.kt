package com.example.kairn.ui.editor.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorPointTest {

    @Test
    fun defaultName_usesOrderPlusOne() {
        val point = EditorPoint(
            id = "p1",
            order = 0,
            latitude = 45.0,
            longitude = 6.0,
        )

        assertEquals("Point 1", point.name)
    }

    @Test
    fun defaultName_isCorrectForHigherOrders() {
        val point = EditorPoint(
            id = "p5",
            order = 4,
            latitude = 45.0,
            longitude = 6.0,
        )

        assertEquals("Point 5", point.name)
    }

    @Test
    fun customName_overridesDefault() {
        val point = EditorPoint(
            id = "p1",
            order = 0,
            latitude = 45.0,
            longitude = 6.0,
            name = "Start Point",
        )

        assertEquals("Start Point", point.name)
    }

    @Test
    fun geoPoint_hasCorrectCoordinates() {
        val point = EditorPoint(
            id = "p1",
            order = 0,
            latitude = 45.907972,
            longitude = 6.103278,
        )

        val geoPoint = point.geoPoint
        assertEquals(45.907972, geoPoint.latitude, 0.000001)
        assertEquals(6.103278, geoPoint.longitude, 0.000001)
    }

    @Test
    fun toGeoPoint_hasCorrectCoordinates() {
        val point = EditorPoint(
            id = "p1",
            order = 0,
            latitude = 48.8566,
            longitude = 2.3522,
        )

        val geoPoint = point.toGeoPoint()
        assertEquals(48.8566, geoPoint.latitude, 0.0001)
        assertEquals(2.3522, geoPoint.longitude, 0.0001)
    }

    @Test
    fun geoPoint_andToGeoPoint_returnEqualValues() {
        val point = EditorPoint(
            id = "p1",
            order = 0,
            latitude = 45.0,
            longitude = 6.0,
        )

        val fromProperty = point.geoPoint
        val fromExtension = point.toGeoPoint()

        assertEquals(fromProperty.latitude, fromExtension.latitude, 0.0)
        assertEquals(fromProperty.longitude, fromExtension.longitude, 0.0)
    }
}

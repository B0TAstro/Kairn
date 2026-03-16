package com.example.kairn.ui.editor.routing

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OsrmDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun osrmResponse_deserializesSuccessfulResponse() {
        val jsonString = """
            {
                "code": "Ok",
                "routes": [
                    {
                        "geometry": {
                            "coordinates": [[6.1, 45.9], [6.2, 46.0]]
                        }
                    }
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString<OsrmResponse>(jsonString)

        assertEquals("Ok", response.code)
        assertNull(response.message)
        assertEquals(1, response.routes.size)
        assertEquals(2, response.routes.first().geometry.coordinates.size)
    }

    @Test
    fun osrmResponse_deserializesErrorResponse() {
        val jsonString = """
            {
                "code": "NoRoute",
                "message": "No route found",
                "routes": []
            }
        """.trimIndent()

        val response = json.decodeFromString<OsrmResponse>(jsonString)

        assertEquals("NoRoute", response.code)
        assertEquals("No route found", response.message)
        assertTrue(response.routes.isEmpty())
    }

    @Test
    fun osrmGeometry_coordinatesAreCorrectlyParsed() {
        val jsonString = """
            {
                "coordinates": [[6.103278, 45.907972], [6.15, 45.92], [6.2, 46.0]]
            }
        """.trimIndent()

        val geometry = json.decodeFromString<OsrmGeometry>(jsonString)

        assertEquals(3, geometry.coordinates.size)
        assertEquals(6.103278, geometry.coordinates[0][0], 0.000001)
        assertEquals(45.907972, geometry.coordinates[0][1], 0.000001)
    }

    @Test
    fun osrmResponse_ignoresUnknownKeys() {
        val jsonString = """
            {
                "code": "Ok",
                "routes": [
                    {
                        "geometry": {
                            "type": "LineString",
                            "coordinates": [[6.1, 45.9]]
                        },
                        "distance": 1234.5,
                        "duration": 600.0
                    }
                ],
                "waypoints": []
            }
        """.trimIndent()

        val response = json.decodeFromString<OsrmResponse>(jsonString)

        assertEquals("Ok", response.code)
        assertEquals(1, response.routes.size)
    }

    @Test
    fun osrmRoute_defaultMessageIsNull() {
        val response = OsrmResponse(
            code = "Ok",
            routes = emptyList(),
        )

        assertNull(response.message)
    }
}

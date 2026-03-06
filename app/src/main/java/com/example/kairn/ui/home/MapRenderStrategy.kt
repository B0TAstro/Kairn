package com.example.kairn.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

enum class MapProvider {
    MAPBOX,
    MAPLIBRE,
}

data class MapCameraState(
    val userLatitude: Double?,
    val userLongitude: Double?,
    val selectedCity: MapCity?,
)

interface MapRenderStrategy {
    @Composable
    fun render(
        cameraState: MapCameraState,
        modifier: Modifier = Modifier,
    )
}

object MapboxRenderStrategy : MapRenderStrategy {
    @Composable
    override fun render(
        cameraState: MapCameraState,
        modifier: Modifier,
    ) {
        MapboxPocMapView(
            modifier = modifier,
            userLatitude = cameraState.userLatitude,
            userLongitude = cameraState.userLongitude,
            selectedCity = cameraState.selectedCity,
        )
    }
}

object MapLibreRenderStrategy : MapRenderStrategy {
    @Composable
    override fun render(
        cameraState: MapCameraState,
        modifier: Modifier,
    ) {
        MapLibrePocMapView(
            modifier = modifier,
            userLatitude = cameraState.userLatitude,
            userLongitude = cameraState.userLongitude,
            selectedCity = cameraState.selectedCity,
        )
    }
}

object MapRenderStrategyFactory {
    fun create(provider: MapProvider): MapRenderStrategy {
        return when (provider) {
            MapProvider.MAPBOX -> MapboxRenderStrategy
            MapProvider.MAPLIBRE -> MapLibreRenderStrategy
        }
    }
}

@Composable
fun StrategyMapView(
    provider: MapProvider,
    cameraState: MapCameraState,
    modifier: Modifier = Modifier,
) {
    val strategy = remember(provider) { MapRenderStrategyFactory.create(provider) }
    strategy.render(
        cameraState = cameraState,
        modifier = modifier,
    )
}

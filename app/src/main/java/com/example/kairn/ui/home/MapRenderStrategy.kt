package com.example.kairn.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.kairn.domain.model.GpxRoute

enum class MapProvider {
    MAPBOX,
    MAPLIBRE,
}

data class MapCameraState(
    val userLatitude: Double?,
    val userLongitude: Double?,
    val selectedCity: MapCity?,
    val gpxRoutes: List<GpxRoute> = emptyList(),
    val selectedGpxRoute: GpxRoute? = null,
)

interface MapRenderStrategy {
    @Composable
    fun render(
        cameraState: MapCameraState,
        modifier: Modifier,
        onGpxRouteClick: (GpxRoute) -> Unit,
    )
}

object MapboxRenderStrategy : MapRenderStrategy {
    @Composable
    override fun render(
        cameraState: MapCameraState,
        modifier: Modifier,
        onGpxRouteClick: (GpxRoute) -> Unit,
    ) {
        MapboxPocMapView(
            modifier = modifier,
            userLatitude = cameraState.userLatitude,
            userLongitude = cameraState.userLongitude,
            selectedCity = cameraState.selectedCity,
            gpxRoutes = cameraState.gpxRoutes,
            selectedGpxRoute = cameraState.selectedGpxRoute,
            onGpxRouteClick = onGpxRouteClick,
        )
    }
}

object MapLibreRenderStrategy : MapRenderStrategy {
    @Composable
    override fun render(
        cameraState: MapCameraState,
        modifier: Modifier,
        onGpxRouteClick: (GpxRoute) -> Unit,
    ) {
        MapLibrePocMapView(
            modifier = modifier,
            userLatitude = cameraState.userLatitude,
            userLongitude = cameraState.userLongitude,
            selectedCity = cameraState.selectedCity,
            gpxRoutes = cameraState.gpxRoutes,
            selectedGpxRoute = cameraState.selectedGpxRoute,
            onGpxRouteClick = onGpxRouteClick,
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
    onGpxRouteClick: (GpxRoute) -> Unit,
) {
    val strategy = remember(provider) { MapRenderStrategyFactory.create(provider) }
    strategy.render(
        cameraState = cameraState,
        modifier = modifier,
        onGpxRouteClick = onGpxRouteClick,
    )
}

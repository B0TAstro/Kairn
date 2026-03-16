package com.example.kairn.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kairn.data.location.LocationService
import com.example.kairn.data.parser.GpxParser
import com.example.kairn.data.repository.GpxRepository
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.model.HikeDifficulty
import com.example.kairn.domain.model.User
import com.example.kairn.domain.repository.AuthRepository
import com.example.kairn.domain.repository.HikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.hypot
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val hikeRepository: HikeRepository,
    private val locationService: LocationService,
    private val gpxRepository: GpxRepository,
    private val gpxParser: GpxParser,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val supportedCities = listOf(
        DEFAULT_HOME_CITY,
        MapCity(name = "Chamonix", latitude = 45.923697, longitude = 6.869433),
        MapCity(name = "Lyon", latitude = 45.764043, longitude = 4.835659),
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var locationCollectionJob: Job? = null
    private var runTrackingJob: Job? = null
    private var activeRunRoutePoints: List<GeoPoint> = emptyList()

    /** Whether the device currently has fine-location permission. */
    val hasLocationPermission: Boolean
        get() = locationService.hasLocationPermission()

    init {
        observeCurrentUser()
        loadHikes()
        loadGpxRoutes()
        collectLocation()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { user ->
                val name = user?.displayName().orEmpty().ifBlank { "Hiker" }
                _uiState.update { it.copy(username = name) }
            }
        }
    }

    private fun loadHikes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            hikeRepository.getHikes()
                .onSuccess { hikes ->
                    _uiState.update { current ->
                        val selectedCity = current.selectedCity ?: DEFAULT_HOME_CITY
                        current.copy(
                            nearbyHikes = hikes,
                            selectedCity = selectedCity,
                            citySuggestions = citySuggestionsForQuery(current.searchQuery),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Unknown error")
                    }
                }
        }
    }

    private fun loadGpxRoutes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGpx = true, gpxError = null) }
            Log.d(TAG, "loadGpxRoutes: starting")

            // 1. Récupérer les métadonnées des hikes depuis Supabase
            val hikesMetadataResult = gpxRepository.getHikesMetadata()
            val hikesMetadata = hikesMetadataResult.getOrDefault(emptyMap())
            Log.d(TAG, "loadGpxRoutes: found ${hikesMetadata.size} hike metadata entries")

            gpxRepository.listGpxFiles()
                .onSuccess { gpxFiles ->
                    Log.d(TAG, "loadGpxRoutes: found ${gpxFiles.size} GPX files")
                    val routes = mutableListOf<com.example.kairn.domain.model.GpxRoute>()

                    for (file in gpxFiles) {
                        Log.d(TAG, "loadGpxRoutes: processing ${file.name}")
                        val contentResult = gpxRepository.downloadGpxContent(file.publicUrl)
                        contentResult.onSuccess { content ->
                            val parseResult = gpxParser.parse(content, file.name)
                            parseResult.onSuccess { gpxRoute ->
                                Log.d(TAG, "loadGpxRoutes: parsed ${gpxRoute.name} with ${gpxRoute.points.size} points")

                                // 2. Chercher les métadonnées du hike correspondant
                                val hikeDto = hikesMetadata[file.name]

                                // 3. Créer un GpxRoute avec les métadonnées mergées
                                val mergedRoute = gpxRoute.copy(
                                    id = hikeDto?.id,
                                    creatorId = hikeDto?.creatorId,
                                    createdAt = hikeDto?.createdAt ?: gpxRoute.createdAt,
                                )

                                routes.add(mergedRoute)
                                Log.d(TAG, "loadGpxRoutes: merged route ${gpxRoute.name} with hike metadata: ${hikeDto != null}")
                            }.onFailure { e ->
                                Log.e(TAG, "loadGpxRoutes: parse error for ${file.name}", e)
                            }
                        }.onFailure { e ->
                            Log.e(TAG, "loadGpxRoutes: download error for ${file.name}", e)
                        }
                    }

                    val seededRoute = buildSeededRouteNearAnnecyBase()
                    routes.add(0, seededRoute)

                    Log.d(TAG, "loadGpxRoutes: finished with ${routes.size} routes (including seeded route)")
                    _uiState.update {
                        it.copy(gpxRoutes = routes, isLoadingGpx = false)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "loadGpxRoutes: list error", error)
                    _uiState.update {
                        it.copy(
                            gpxRoutes = listOf(buildSeededRouteNearAnnecyBase()),
                            isLoadingGpx = false,
                            gpxError = error.message,
                        )
                    }
                }
        }
    }

    /**
     * Starts collecting GPS updates. Called once in init; if permission is not yet
     * granted the flow completes immediately and [onPermissionGranted] re-triggers it.
     */
    private fun collectLocation() {
        if (!locationService.hasLocationPermission()) return
        if (locationCollectionJob?.isActive == true) return

        locationCollectionJob = viewModelScope.launch {
            locationService.locationUpdates().collect {
                _uiState.update {
                    if (it.isRunActive) {
                        return@update it
                    }
                    it.copy(
                        userLatitude = ANNECY_AUSSEDAT_LATITUDE,
                        userLongitude = ANNECY_AUSSEDAT_LONGITUDE,
                        location = ANNECY_AUSSEDAT_LABEL,
                    )
                }
            }
        }
    }

    /** Called by the UI after the user grants location permission at runtime. */
    fun onPermissionGranted() {
        collectLocation()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                citySuggestions = citySuggestionsForQuery(query),
            )
        }
    }

    fun onCitySelected(cityName: String) {
        val city = supportedCities.firstOrNull { it.name.equals(cityName, ignoreCase = true) } ?: return
        _uiState.update { current ->
            current.copy(
                searchQuery = city.name,
                location = city.name,
                selectedCity = city,
                citySuggestions = emptyList(),
            )
        }
    }

    fun onDifficultySelected(difficulty: HikeDifficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
    }

    fun onHikeSelected(hike: Hike) {
        _uiState.update { it.copy(selectedHike = hike, isBottomSheetExpanded = true) }
    }

    fun onBottomSheetDismissed() {
        _uiState.update { it.copy(selectedHike = null, isBottomSheetExpanded = false) }
    }

    fun onStartRunFromSelectedHike() {
        val state = _uiState.value
        val title = state.selectedHike?.title ?: "Randonnee locale"
        val route = state.selectedGpxRoute ?: state.gpxRoutes.firstOrNull() ?: buildSeededRouteNearAnnecyBase()
        startRunTracking(title, route)
        _uiState.update { it.copy(selectedHike = null, isBottomSheetExpanded = false) }
    }

    fun onGpxRouteSelected(route: com.example.kairn.domain.model.GpxRoute) {
        _uiState.update { it.copy(selectedGpxRoute = route, isGpxBottomSheetExpanded = true) }
    }

    fun onGpxBottomSheetDismissed() {
        _uiState.update { it.copy(selectedGpxRoute = null, isGpxBottomSheetExpanded = false) }
    }

    fun onStartRunFromSelectedGpx() {
        val route = _uiState.value.selectedGpxRoute ?: buildSeededRouteNearAnnecyBase()
        val title = route.name
        startRunTracking(title, route)
        _uiState.update { it.copy(isGpxBottomSheetExpanded = false) }
    }

    fun stopRunTracking() {
        runTrackingJob?.cancel()
        runTrackingJob = null
        activeRunRoutePoints = emptyList()
        _uiState.update {
            it.copy(
                isRunActive = false,
                activeRunTitle = "",
                activeRunProgress = 0f,
                activeRunDistanceKm = 0.0,
                activeRunElapsedMinutes = 0,
                selectedGpxRoute = null,
            )
        }
    }

    fun acknowledgeRunCompletion() {
        _uiState.update {
            it.copy(
                isRunCompleted = false,
                completedRunTitle = "",
                completedRunDistanceKm = 0.0,
                completedRunElapsedMinutes = 0,
                completedRunXpGained = 0,
            )
        }
    }

    fun retry() {
        loadHikes()
    }

    private fun startRunTracking(
        title: String,
        route: com.example.kairn.domain.model.GpxRoute,
    ) {
        runTrackingJob?.cancel()
        activeRunRoutePoints = if (route.points.size >= 2) route.points else buildSeededRouteNearAnnecyBase().points
        val startPoint = activeRunRoutePoints.firstOrNull() ?: GeoPoint(ANNECY_AUSSEDAT_LATITUDE, ANNECY_AUSSEDAT_LONGITUDE)

        _uiState.update {
            it.copy(
                isRunActive = true,
                activeRunTitle = title,
                activeRunProgress = 0.02f,
                activeRunDistanceKm = 0.1,
                activeRunElapsedMinutes = 1,
                userLatitude = startPoint.latitude,
                userLongitude = startPoint.longitude,
                selectedCity = null,
                selectedGpxRoute = route,
                isRunCompleted = false,
                completedRunTitle = "",
                completedRunDistanceKm = 0.0,
                completedRunElapsedMinutes = 0,
                completedRunXpGained = 0,
            )
        }

        runTrackingJob = viewModelScope.launch {
            while (true) {
                delay(1200)
                var reachedGoal = false
                _uiState.update { current ->
                    if (!current.isRunActive) {
                        return@update current
                    }

                    val nextProgress = (current.activeRunProgress + 0.05f).coerceAtMost(1f)
                    reachedGoal = nextProgress >= 1f
                    val nextPosition = positionAlongRoute(activeRunRoutePoints, nextProgress)
                    val distanceKm = (nextProgress.toDouble() * 6.2).coerceAtMost(6.2)
                    val nextMinutes = current.activeRunElapsedMinutes + 2
                    val xp = ((distanceKm * 35.0) + nextMinutes * 1.5).toInt()
                    current.copy(
                        activeRunProgress = nextProgress,
                        activeRunDistanceKm = distanceKm,
                        activeRunElapsedMinutes = nextMinutes,
                        userLatitude = nextPosition.latitude,
                        userLongitude = nextPosition.longitude,
                        isRunActive = !reachedGoal,
                        isRunCompleted = reachedGoal,
                        completedRunTitle = if (reachedGoal) current.activeRunTitle else current.completedRunTitle,
                        completedRunDistanceKm = if (reachedGoal) distanceKm else current.completedRunDistanceKm,
                        completedRunElapsedMinutes = if (reachedGoal) nextMinutes else current.completedRunElapsedMinutes,
                        completedRunXpGained = if (reachedGoal) xp else current.completedRunXpGained,
                    )
                }
                if (reachedGoal) {
                    break
                }
            }
        }
    }

    private fun positionAlongRoute(points: List<GeoPoint>, progress: Float): GeoPoint {
        if (points.isEmpty()) return GeoPoint(ANNECY_AUSSEDAT_LATITUDE, ANNECY_AUSSEDAT_LONGITUDE)
        if (points.size == 1) return points.first()

        val boundedProgress = progress.coerceIn(0f, 1f)
        val segmentLengths = mutableListOf<Double>()
        var totalLength = 0.0

        for (index in 0 until points.lastIndex) {
            val start = points[index]
            val end = points[index + 1]
            val length = hypot(end.latitude - start.latitude, end.longitude - start.longitude)
            segmentLengths.add(length)
            totalLength += length
        }

        if (totalLength <= 0.0) return points.first()

        val targetDistance = totalLength * boundedProgress
        var traversed = 0.0

        for (index in segmentLengths.indices) {
            val segmentLength = segmentLengths[index]
            val start = points[index]
            val end = points[index + 1]
            if (traversed + segmentLength >= targetDistance) {
                val ratio = ((targetDistance - traversed) / segmentLength).coerceIn(0.0, 1.0)
                return GeoPoint(
                    start.latitude + (end.latitude - start.latitude) * ratio,
                    start.longitude + (end.longitude - start.longitude) * ratio,
                )
            }
            traversed += segmentLength
        }

        return points.last()
    }

    private fun citySuggestionsForQuery(query: String): List<MapCity> {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) return emptyList()
        return supportedCities.filter { it.name.lowercase().contains(normalized) }
    }

    private fun User.displayName(): String {
        return pseudo
            ?: username
            ?: listOfNotNull(firstName, lastName)
                .joinToString(" ")
                .trim()
                .ifBlank { null }
            ?: email.substringBefore('@')
    }

    private fun buildSeededRouteNearAnnecyBase(): com.example.kairn.domain.model.GpxRoute {
        val lat = ANNECY_AUSSEDAT_LATITUDE
        val lon = ANNECY_AUSSEDAT_LONGITUDE
        return com.example.kairn.domain.model.GpxRoute(
            id = "annecy-centre-loop",
            name = "Boucle Cran-Gevrier",
            points = listOf(
                GeoPoint(lat + 0.0010, lon - 0.0018),
                GeoPoint(lat + 0.0016, lon - 0.0006),
                GeoPoint(lat + 0.0013, lon + 0.0011),
                GeoPoint(lat + 0.0002, lon + 0.0018),
                GeoPoint(lat - 0.0010, lon + 0.0011),
                GeoPoint(lat - 0.0015, lon - 0.0004),
                GeoPoint(lat - 0.0006, lon - 0.0019),
                GeoPoint(lat + 0.0010, lon - 0.0018),
            ),
            createdAt = "2026-03-16T10:00:00",
            creatorId = "kairn-team",
            distanceMeters = 6200.0,
            fileName = "annecy-centre-loop.gpx",
        )
    }
}

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
    private var demoRunJob: Job? = null

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

                    val demoRoute = buildDemoRouteNearAnnecyBase()
                    routes.add(0, demoRoute)

                    Log.d(TAG, "loadGpxRoutes: finished with ${routes.size} routes (including demo route)")
                    _uiState.update {
                        it.copy(gpxRoutes = routes, isLoadingGpx = false)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "loadGpxRoutes: list error", error)
                    _uiState.update {
                        it.copy(
                            gpxRoutes = listOf(buildDemoRouteNearAnnecyBase()),
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

    fun onStartHikeDemoFromSelectedHike() {
        val title = _uiState.value.selectedHike?.title ?: "Randonnee locale"
        startHikeDemo(title)
        onBottomSheetDismissed()
    }

    fun onGpxRouteSelected(route: com.example.kairn.domain.model.GpxRoute) {
        _uiState.update { it.copy(selectedGpxRoute = route, isGpxBottomSheetExpanded = true) }
    }

    fun onGpxBottomSheetDismissed() {
        _uiState.update { it.copy(selectedGpxRoute = null, isGpxBottomSheetExpanded = false) }
    }

    fun onStartHikeDemoFromSelectedGpx() {
        val title = _uiState.value.selectedGpxRoute?.name ?: "Trace GPX"
        startHikeDemo(title)
        onGpxBottomSheetDismissed()
    }

    fun stopHikeDemo() {
        demoRunJob?.cancel()
        demoRunJob = null
        _uiState.update {
            it.copy(
                isDemoRunActive = false,
                demoRunTitle = "",
                demoRunProgress = 0f,
                demoRunDistanceKm = 0.0,
                demoRunElapsedMinutes = 0,
            )
        }
    }

    fun retry() {
        loadHikes()
    }

    private fun startHikeDemo(title: String) {
        demoRunJob?.cancel()
        _uiState.update {
            it.copy(
                isDemoRunActive = true,
                demoRunTitle = title,
                demoRunProgress = 0.02f,
                demoRunDistanceKm = 0.1,
                demoRunElapsedMinutes = 1,
            )
        }

        demoRunJob = viewModelScope.launch {
            while (true) {
                delay(1500)
                var reachedGoal = false
                _uiState.update { current ->
                    if (!current.isDemoRunActive) {
                        return@update current
                    }

                    val nextProgress = (current.demoRunProgress + 0.06f).coerceAtMost(1f)
                    reachedGoal = nextProgress >= 1f
                    current.copy(
                        demoRunProgress = nextProgress,
                        demoRunDistanceKm = (nextProgress.toDouble() * 6.2).coerceAtMost(6.2),
                        demoRunElapsedMinutes = current.demoRunElapsedMinutes + 2,
                    )
                }
                if (reachedGoal) {
                    break
                }
            }
        }
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

    private fun buildDemoRouteNearAnnecyBase(): com.example.kairn.domain.model.GpxRoute {
        val lat = ANNECY_AUSSEDAT_LATITUDE
        val lon = ANNECY_AUSSEDAT_LONGITUDE
        return com.example.kairn.domain.model.GpxRoute(
            id = "demo-annecy-loop",
            name = "Boucle Annecy Centre",
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
            creatorId = "kairn-demo",
            distanceMeters = 6200.0,
            fileName = "demo-annecy-loop.gpx",
        )
    }
}

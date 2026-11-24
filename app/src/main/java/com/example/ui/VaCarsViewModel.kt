package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Car
import com.example.data.CarRepository
import com.example.data.FleetDataSource
import com.example.data.SampleFleet
import com.example.data.SupabaseConfig
import com.example.data.SupabaseConfigStore
import com.example.data.ThemePreference
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FleetFilter(val label: String) {
    ALL("All Cars"),
    AVAILABLE("Available"),
    AUTOMATIC("Automatic"),
    MANUAL("Manual"),
    PETROL("Petrol"),
    DIESEL("Diesel"),
    SEVEN_SEATER("7-Seater")
}

data class VaCarsUiState(
    val allCars: List<Car> = SampleFleet.cars,
    val searchQuery: String = "",
    val selectedFilter: FleetFilter = FleetFilter.ALL,
    val dataSource: FleetDataSource = FleetDataSource.SAMPLE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val config: SupabaseConfig = SupabaseConfig(),
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val isSetupModalOpen: Boolean = false
) {
    val filteredCars: List<Car>
        get() {
            val query = searchQuery.trim()
            return allCars.filter { car ->
                val matchesQuery = query.isEmpty() ||
                    car.carName.contains(query, ignoreCase = true) ||
                    (car.fuelType?.contains(query, ignoreCase = true) == true) ||
                    (car.transmission?.contains(query, ignoreCase = true) == true)

                val matchesFilter = when (selectedFilter) {
                    FleetFilter.ALL -> true
                    FleetFilter.AVAILABLE -> car.isAvailable
                    FleetFilter.AUTOMATIC -> car.transmission?.contains("auto", ignoreCase = true) == true
                    FleetFilter.MANUAL -> car.transmission?.contains("manual", ignoreCase = true) == true
                    FleetFilter.PETROL -> car.fuelType?.contains("petrol", ignoreCase = true) == true
                    FleetFilter.DIESEL -> car.fuelType?.contains("diesel", ignoreCase = true) == true
                    FleetFilter.SEVEN_SEATER -> (car.seats ?: 0) >= 7
                }
                matchesQuery && matchesFilter
            }
        }
}

class VaCarsViewModel(application: Application) : AndroidViewModel(application) {

    private val configStore = SupabaseConfigStore(application.applicationContext)
    private val repository = CarRepository(configStore)

    private val _uiState = MutableStateFlow(VaCarsUiState())
    val uiState: StateFlow<VaCarsUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            configStore.themePreferenceFlow.collectLatest { pref ->
                _uiState.update { it.copy(themePreference = pref) }
            }
        }
        viewModelScope.launch {
            configStore.configFlow.collectLatest { config ->
                _uiState.update { it.copy(config = config) }
                fetchFleet(config, showSuccessToast = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(filter: FleetFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun clearSearchAndFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedFilter = FleetFilter.ALL) }
    }

    fun refreshFleet() {
        val currentConfig = _uiState.value.config
        viewModelScope.launch {
            fetchFleet(currentConfig, showSuccessToast = true)
        }
    }

    fun openSetupModal() {
        _uiState.update { it.copy(isSetupModalOpen = true) }
    }

    fun closeSetupModal() {
        _uiState.update { it.copy(isSetupModalOpen = false) }
    }

    fun saveSupabaseConfig(url: String, anonKey: String, whatsappNumber: String) {
        viewModelScope.launch {
            configStore.saveConfig(url = url, anonKey = anonKey, whatsappNumber = whatsappNumber)
            _uiState.update { it.copy(isSetupModalOpen = false) }
            val updatedConfig = SupabaseConfig(
                url = SupabaseConfig.normalizeUrl(url),
                anonKey = anonKey.trim(),
                whatsappNumber = whatsappNumber.filter { it.isDigit() },
                isFromUserOverride = true
            )
            fetchFleet(updatedConfig, showSuccessToast = true)
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            configStore.clearSupabaseKeys()
            _uiState.update {
                it.copy(
                    isSetupModalOpen = false,
                    allCars = SampleFleet.cars,
                    dataSource = FleetDataSource.SAMPLE,
                    errorMessage = null
                )
            }
            _snackbarEvents.tryEmit("Switched to VA CARS sample fleet.")
        }
    }

    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch {
            val next = if (currentlyDark) ThemePreference.LIGHT else ThemePreference.DARK
            configStore.saveThemePreference(next)
        }
    }

    fun findCarById(carId: String): Car? {
        return _uiState.value.allCars.firstOrNull { it.id == carId }
    }

    fun notifyMessage(message: String) {
        _snackbarEvents.tryEmit(message)
    }

    private suspend fun fetchFleet(config: SupabaseConfig, showSuccessToast: Boolean) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = repository.loadFleet(config)
        _uiState.update {
            it.copy(
                allCars = result.cars,
                dataSource = result.source,
                isLoading = false,
                errorMessage = result.errorMessage
            )
        }
        if (showSuccessToast) {
            if (result.errorMessage != null) {
                _snackbarEvents.tryEmit(result.errorMessage)
            } else if (result.source == FleetDataSource.LIVE_SUPABASE) {
                _snackbarEvents.tryEmit("Synced ${result.cars.size} cars from live Supabase.")
            } else {
                _snackbarEvents.tryEmit("Loaded sample fleet (${result.cars.size} cars).")
            }
        }
    }
}

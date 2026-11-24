package com.example.data

enum class FleetDataSource {
    SAMPLE,
    LIVE_SUPABASE
}

data class FleetResult(
    val cars: List<Car>,
    val source: FleetDataSource,
    val errorMessage: String? = null
)

class CarRepository(
    val configStore: SupabaseConfigStore,
    private val supabaseService: SupabaseService = SupabaseService()
) {
    suspend fun loadFleet(config: SupabaseConfig): FleetResult {
        if (!config.isConfigured) {
            return FleetResult(
                cars = SampleFleet.cars,
                source = FleetDataSource.SAMPLE,
                errorMessage = null
            )
        }

        val remoteResult = supabaseService.fetchCars(config.url, config.anonKey)
        return remoteResult.fold(
            onSuccess = { remoteCars ->
                FleetResult(
                    cars = remoteCars,
                    source = FleetDataSource.LIVE_SUPABASE,
                    errorMessage = null
                )
            },
            onFailure = { error ->
                FleetResult(
                    cars = SampleFleet.cars,
                    source = FleetDataSource.SAMPLE,
                    errorMessage = error.message ?: "Failed to fetch live Supabase fleet."
                )
            }
        )
    }
}

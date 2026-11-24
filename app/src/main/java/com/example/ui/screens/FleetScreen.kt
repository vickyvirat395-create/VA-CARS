package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Car
import com.example.ui.FleetFilter
import com.example.ui.VaCarsUiState
import com.example.ui.components.CarCard
import com.example.ui.components.EmptyFleetState
import com.example.ui.components.FleetFilterRow
import com.example.ui.components.FleetSearchBar
import com.example.ui.components.SupabaseErrorBanner

@Composable
fun FleetScreen(
    uiState: VaCarsUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (FleetFilter) -> Unit,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit,
    onCarClick: (Car) -> Unit,
    onOpenSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cars = uiState.filteredCars

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 720.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            item {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.section_all_fleet),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${cars.size} of ${uiState.allCars.size} vehicles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("fleet_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.cd_refresh)
                            )
                        }
                    }

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SupabaseErrorBanner(
                            errorMessage = uiState.errorMessage,
                            onOpenSetup = onOpenSetup,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FleetSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FleetFilterRow(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = onFilterSelected
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (cars.isEmpty()) {
                item {
                    EmptyFleetState(onClearFilters = onClearFilters)
                }
            } else if (isWide) {
                val rows = cars.chunked(2)
                items(rows) { rowCars ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowCars.forEach { car ->
                            CarCard(
                                car = car,
                                onClick = { onCarClick(car) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowCars.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(cars, key = { it.id }) { car ->
                    CarCard(
                        car = car,
                        onClick = { onCarClick(car) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

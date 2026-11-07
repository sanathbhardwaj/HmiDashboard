package com.example.hmidashboard.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hmidashboard.data.VehicleDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Exposes the dashboard state as a [StateFlow]. The upstream simulation only runs while the UI
 * is collecting (plus a 5 s grace period that survives configuration changes).
 */
class VehicleViewModel(
    source: VehicleDataSource,
    staleTimeoutMs: Long = DEFAULT_STALE_TIMEOUT_MS,
) : ViewModel() {

    val uiState: StateFlow<VehicleUiState> = source.data
        .toUiStates(staleTimeoutMs)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = VehicleUiState.Waiting,
        )

    companion object {
        /** Must comfortably exceed the 1 s sample interval. */
        const val DEFAULT_STALE_TIMEOUT_MS = 3_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}

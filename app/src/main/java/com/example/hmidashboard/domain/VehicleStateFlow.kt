package com.example.hmidashboard.domain

import com.example.hmidashboard.data.VehicleData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest

/**
 * Turns raw samples into UI states and adds staleness detection.
 *
 * Every sample (or the start of collection) arms a timer; if no newer sample arrives within
 * [staleTimeoutMs] a [VehicleUiState.Stale] is emitted. `transformLatest` cancels the pending
 * timer whenever a fresh sample arrives, so a healthy feed never goes stale. If the source
 * throws, the error is swallowed and the dashboard degrades to Stale instead of crashing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<VehicleData>.toUiStates(staleTimeoutMs: Long): Flow<VehicleUiState> {
    val samples: Flow<VehicleData?> = this
    return samples
        .catch { /* source failure: fall through to the stale timer */ }
        .onStart { emit(null) }
        .transformLatest { sample ->
            if (sample != null) emit(VehicleUiStateMapper.map(sample))
            delay(staleTimeoutMs)
            emit(VehicleUiState.Stale)
        }
}

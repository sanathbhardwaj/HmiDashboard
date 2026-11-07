package com.example.hmidashboard.data

/**
 * Raw vehicle signals as a data provider would deliver them.
 *
 * Any field may be `null` (signal unavailable) and [speedKmh] may be out of range
 * (negative, NaN, absurdly large). Interpretation and validation happen in the
 * domain layer, never here.
 */
data class VehicleData(
    val speedKmh: Float?,
    val motorOn: Boolean?,
    val killSwitchActive: Boolean?,
)

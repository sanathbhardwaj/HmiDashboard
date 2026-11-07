package com.example.hmidashboard.data

import kotlinx.coroutines.flow.Flow

/** Source of raw vehicle data. A real CAN/BLE implementation could replace the simulator. */
interface VehicleDataSource {
    val data: Flow<VehicleData>
}

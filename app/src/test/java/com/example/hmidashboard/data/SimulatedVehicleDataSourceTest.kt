package com.example.hmidashboard.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedVehicleDataSourceTest {

    @Test
    fun `emits the five scenarios in order, one per tick`() = runTest {
        val source = SimulatedVehicleDataSource(tickMs = 1_000, includeEdgeCases = false, ticksPerScenario = 1)

        val emitted = source.data.take(5).toList()

        assertEquals(
            listOf(
                VehicleData(0f, motorOn = false, killSwitchActive = false),
                VehicleData(0f, motorOn = true, killSwitchActive = false),
                VehicleData(25f, motorOn = true, killSwitchActive = false),
                VehicleData(50f, motorOn = true, killSwitchActive = false),
                VehicleData(0f, motorOn = false, killSwitchActive = true),
            ),
            emitted,
        )
        assertEquals(4_000L, currentTime)
    }

    @Test
    fun `edge cases add an invalid sample and a silent dropout before looping`() = runTest {
        val source = SimulatedVehicleDataSource(tickMs = 1_000, includeEdgeCases = true, ticksPerScenario = 1)

        val emitted = source.data.take(7).toList()

        assertEquals(-15f, emitted[5].speedKmh)
        assertEquals(VehicleData(0f, motorOn = false, killSwitchActive = false), emitted[6])
        // 6 samples (t=0..5s), then 5 silent ticks of dropout before the loop restarts.
        assertEquals(11_000L, currentTime)
    }
}

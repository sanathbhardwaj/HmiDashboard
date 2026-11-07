package com.example.hmidashboard.domain

import com.example.hmidashboard.MainDispatcherRule
import com.example.hmidashboard.data.VehicleData
import com.example.hmidashboard.data.VehicleDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeSource : VehicleDataSource {
        val flow = MutableSharedFlow<VehicleData>(replay = 1)
        override val data: Flow<VehicleData> = flow
    }

    @Test
    fun `initial state is waiting`() = runTest {
        val vm = VehicleViewModel(FakeSource())
        assertEquals(VehicleUiState.Waiting, vm.uiState.value)
    }

    @Test
    fun `uiState follows the source through the full scenario sequence`() = runTest {
        val source = FakeSource()
        val vm = VehicleViewModel(source)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { } }
        runCurrent()

        val sequence = listOf(
            VehicleData(0f, false, false) to RideState.PARKED,
            VehicleData(0f, true, false) to RideState.READY,
            VehicleData(25f, true, false) to RideState.RIDING,
            VehicleData(50f, true, false) to RideState.ACCELERATING,
            VehicleData(0f, false, true) to RideState.MOTOR_DISABLED,
        )
        for ((sample, expected) in sequence) {
            source.flow.emit(sample)
            runCurrent()
            assertEquals(expected, vm.uiState.value.rideState)
        }
    }

    @Test
    fun `uiState shows invalid speed gracefully and goes stale when data stops`() = runTest {
        val source = FakeSource()
        val vm = VehicleViewModel(source, staleTimeoutMs = 3_000)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { } }
        runCurrent()

        source.flow.emit(VehicleData(-15f, motorOn = true, killSwitchActive = false))
        runCurrent()
        assertEquals(SpeedStatus.INVALID, vm.uiState.value.speedStatus)
        assertEquals(null, vm.uiState.value.speedKmh)

        advanceTimeBy(3_000)
        runCurrent()
        assertEquals(VehicleUiState.Stale, vm.uiState.value)
    }
}

package com.example.hmidashboard.domain

import com.example.hmidashboard.data.VehicleData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleStateFlowTest {

    private val riding = VehicleData(25f, motorOn = true, killSwitchActive = false)
    private val accelerating = VehicleData(50f, motorOn = true, killSwitchActive = false)

    @Test
    fun `each sample is mapped to a new live state`() = runTest {
        val source = MutableSharedFlow<VehicleData>(replay = 1)
        val states = mutableListOf<VehicleUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            source.toUiStates(3_000).collect { states.add(it) }
        }
        runCurrent()

        source.emit(riding)
        runCurrent()
        source.emit(accelerating)
        runCurrent()

        assertEquals(listOf(RideState.RIDING, RideState.ACCELERATING), states.map { it.rideState })
        assertEquals(listOf(LinkStatus.LIVE, LinkStatus.LIVE), states.map { it.link })
        job.cancel()
    }

    @Test
    fun `state goes stale after timeout and recovers on next sample`() = runTest {
        val source = MutableSharedFlow<VehicleData>(replay = 1)
        val states = mutableListOf<VehicleUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            source.toUiStates(3_000).collect { states.add(it) }
        }
        runCurrent()
        source.emit(riding)
        runCurrent()

        advanceTimeBy(2_999)
        runCurrent()
        assertEquals(LinkStatus.LIVE, states.last().link)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(VehicleUiState.Stale, states.last())
        assertEquals(null, states.last().speedKmh)

        source.emit(accelerating)
        runCurrent()
        assertEquals(LinkStatus.LIVE, states.last().link)
        assertEquals(50, states.last().speedKmh)
        job.cancel()
    }

    @Test
    fun `no data at all becomes stale instead of waiting forever`() = runTest {
        val source = MutableSharedFlow<VehicleData>()
        val states = mutableListOf<VehicleUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            source.toUiStates(3_000).collect { states.add(it) }
        }
        runCurrent()
        advanceTimeBy(2_999)
        runCurrent()
        assertEquals(emptyList<VehicleUiState>(), states)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(VehicleUiState.Stale), states)
        job.cancel()
    }

    @Test
    fun `source failure degrades to stale and does not throw`() = runTest {
        val source = flow<VehicleData> {
            emit(riding)
            throw IllegalStateException("sensor bus failure")
        }
        val states = mutableListOf<VehicleUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            source.toUiStates(1_000).collect { states.add(it) }
        }
        advanceUntilIdle()

        assertEquals(listOf(LinkStatus.LIVE, LinkStatus.STALE), states.map { it.link })
        job.cancel()
    }
}

package com.example.hmidashboard.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Cold flow that loops through the assignment scenarios, emitting one sample every [tickMs].
 *
 * Each scenario is held for [ticksPerScenario] samples so it stays on screen long enough to read.
 * When [includeEdgeCases] is true the loop also contains an invalid-speed sample and a
 * signal dropout (no emissions at all), so the graceful-degradation paths can be seen live.
 */
class SimulatedVehicleDataSource(
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val includeEdgeCases: Boolean = true,
    private val ticksPerScenario: Int = DEFAULT_TICKS_PER_SCENARIO,
) : VehicleDataSource {

    private class Step(val sample: VehicleData?, val ticks: Int)

    private val steps: List<Step> = buildList {
        add(Step(VehicleData(0f, motorOn = false, killSwitchActive = false), ticksPerScenario)) // Parked
        add(Step(VehicleData(0f, motorOn = true, killSwitchActive = false), ticksPerScenario)) // Ready
        add(Step(VehicleData(25f, motorOn = true, killSwitchActive = false), ticksPerScenario)) // Riding
        add(Step(VehicleData(50f, motorOn = true, killSwitchActive = false), ticksPerScenario)) // Accelerating
        add(Step(VehicleData(0f, motorOn = false, killSwitchActive = true), ticksPerScenario)) // Motor disabled
        if (includeEdgeCases) {
            add(Step(VehicleData(-15f, motorOn = true, killSwitchActive = false), ticksPerScenario)) // Invalid speed
            add(Step(null, DROPOUT_TICKS)) // Signal dropout: nothing emitted
        }
    }

    override val data: Flow<VehicleData> = flow {
        while (true) {
            for (step in steps) {
                repeat(step.ticks) {
                    step.sample?.let { emit(it) }
                    delay(tickMs)
                }
            }
        }
    }

    companion object {
        const val DEFAULT_TICK_MS = 1_000L
        const val DEFAULT_TICKS_PER_SCENARIO = 3
        const val DROPOUT_TICKS = 5
    }
}

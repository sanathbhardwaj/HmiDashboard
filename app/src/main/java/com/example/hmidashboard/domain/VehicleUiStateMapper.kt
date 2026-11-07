package com.example.hmidashboard.domain

import com.example.hmidashboard.data.VehicleData
import kotlin.math.roundToInt

/** Pure translation of raw [VehicleData] into a validated [VehicleUiState]. */
object VehicleUiStateMapper {

    /** Anything above this is treated as a corrupt reading, not a real speed. */
    const val MAX_VALID_SPEED_KMH = 200f

    /** At or above this speed (with motor on) the ride state is ACCELERATING. */
    const val ACCELERATING_THRESHOLD_KMH = 40

    fun map(data: VehicleData): VehicleUiState {
        val raw = data.speedKmh
        val speedKmh: Int?
        val speedStatus: SpeedStatus
        when {
            raw == null -> {
                speedKmh = null
                speedStatus = SpeedStatus.MISSING
            }
            raw.isNaN() || raw.isInfinite() || raw < 0f || raw > MAX_VALID_SPEED_KMH -> {
                speedKmh = null
                speedStatus = SpeedStatus.INVALID
            }
            else -> {
                speedKmh = raw.roundToInt()
                speedStatus = SpeedStatus.OK
            }
        }

        val motor = when (data.motorOn) {
            true -> MotorStatus.ON
            false -> MotorStatus.OFF
            null -> MotorStatus.UNKNOWN
        }
        val killSwitch = when (data.killSwitchActive) {
            true -> KillSwitchStatus.ACTIVE
            false -> KillSwitchStatus.INACTIVE
            null -> KillSwitchStatus.UNKNOWN
        }

        return VehicleUiState(
            speedKmh = speedKmh,
            speedStatus = speedStatus,
            motor = motor,
            killSwitch = killSwitch,
            rideState = deriveRideState(speedKmh, motor, killSwitch),
            link = LinkStatus.LIVE,
        )
    }

    private fun deriveRideState(
        speedKmh: Int?,
        motor: MotorStatus,
        killSwitch: KillSwitchStatus,
    ): RideState {
        if (killSwitch == KillSwitchStatus.ACTIVE) return RideState.MOTOR_DISABLED
        if (speedKmh == null || motor == MotorStatus.UNKNOWN || killSwitch == KillSwitchStatus.UNKNOWN) {
            return RideState.UNAVAILABLE
        }
        return when (motor) {
            MotorStatus.OFF -> if (speedKmh == 0) RideState.PARKED else RideState.COASTING
            MotorStatus.ON -> when {
                speedKmh == 0 -> RideState.READY
                speedKmh >= ACCELERATING_THRESHOLD_KMH -> RideState.ACCELERATING
                else -> RideState.RIDING
            }
            MotorStatus.UNKNOWN -> RideState.UNAVAILABLE
        }
    }
}

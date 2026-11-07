package com.example.hmidashboard.domain

enum class MotorStatus { ON, OFF, UNKNOWN }

enum class KillSwitchStatus { ACTIVE, INACTIVE, UNKNOWN }

enum class RideState {
    PARKED,
    READY,
    RIDING,
    ACCELERATING,

    /** Motor off but still moving. Motor OFF does not imply zero speed. */
    COASTING,
    MOTOR_DISABLED,
    UNAVAILABLE,
}

/** Why [VehicleUiState.speedKmh] is null. */
enum class SpeedStatus { OK, INVALID, MISSING }

/** Freshness of the data feed. */
enum class LinkStatus { WAITING, LIVE, STALE }

/** Everything the dashboard needs to render. Immutable, so the UI is a pure function of it. */
data class VehicleUiState(
    val speedKmh: Int?,
    val speedStatus: SpeedStatus,
    val motor: MotorStatus,
    val killSwitch: KillSwitchStatus,
    val rideState: RideState,
    val link: LinkStatus,
) {
    companion object {
        /** Before the first sample arrives. */
        val Waiting = VehicleUiState(
            speedKmh = null,
            speedStatus = SpeedStatus.MISSING,
            motor = MotorStatus.UNKNOWN,
            killSwitch = KillSwitchStatus.UNKNOWN,
            rideState = RideState.UNAVAILABLE,
            link = LinkStatus.WAITING,
        )

        /** No fresh data: every value is shown as unknown rather than a stale number. */
        val Stale = Waiting.copy(link = LinkStatus.STALE)
    }
}

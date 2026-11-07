package com.example.hmidashboard.domain

import com.example.hmidashboard.data.VehicleData
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleUiStateMapperTest {

    private fun map(speed: Float?, motor: Boolean?, kill: Boolean?) =
        VehicleUiStateMapper.map(VehicleData(speed, motor, kill))

    @Test
    fun `parked - 0 kmh, motor off, kill switch inactive`() {
        val s = map(0f, motor = false, kill = false)
        assertEquals(0, s.speedKmh)
        assertEquals(MotorStatus.OFF, s.motor)
        assertEquals(KillSwitchStatus.INACTIVE, s.killSwitch)
        assertEquals(RideState.PARKED, s.rideState)
        assertEquals(LinkStatus.LIVE, s.link)
    }

    @Test
    fun `ready - 0 kmh, motor on`() {
        val s = map(0f, motor = true, kill = false)
        assertEquals(MotorStatus.ON, s.motor)
        assertEquals(RideState.READY, s.rideState)
    }

    @Test
    fun `riding - 25 kmh`() {
        val s = map(25f, motor = true, kill = false)
        assertEquals(25, s.speedKmh)
        assertEquals(RideState.RIDING, s.rideState)
    }

    @Test
    fun `accelerating - 50 kmh`() {
        val s = map(50f, motor = true, kill = false)
        assertEquals(50, s.speedKmh)
        assertEquals(RideState.ACCELERATING, s.rideState)
    }

    @Test
    fun `motor disabled - kill switch active`() {
        val s = map(0f, motor = false, kill = true)
        assertEquals(KillSwitchStatus.ACTIVE, s.killSwitch)
        assertEquals(MotorStatus.OFF, s.motor)
        assertEquals(RideState.MOTOR_DISABLED, s.rideState)
    }

    @Test
    fun `kill switch active keeps showing real speed while still moving`() {
        val s = map(30f, motor = false, kill = true)
        assertEquals(30, s.speedKmh)
        assertEquals(RideState.MOTOR_DISABLED, s.rideState)
    }

    @Test
    fun `motor off with speed above zero is coasting, not forced to zero`() {
        val s = map(18f, motor = false, kill = false)
        assertEquals(18, s.speedKmh)
        assertEquals(RideState.COASTING, s.rideState)
    }

    @Test
    fun `negative speed is invalid and hidden`() {
        val s = map(-15f, motor = true, kill = false)
        assertEquals(null, s.speedKmh)
        assertEquals(SpeedStatus.INVALID, s.speedStatus)
        assertEquals(MotorStatus.ON, s.motor)
        assertEquals(RideState.UNAVAILABLE, s.rideState)
    }

    @Test
    fun `NaN, infinite and absurd speeds are invalid`() {
        listOf(Float.NaN, Float.POSITIVE_INFINITY, 5_000f).forEach { bad ->
            val s = map(bad, motor = true, kill = false)
            assertEquals("speed=$bad", null, s.speedKmh)
            assertEquals("speed=$bad", SpeedStatus.INVALID, s.speedStatus)
        }
    }

    @Test
    fun `missing signals map to unknown without crashing`() {
        val s = map(null, motor = null, kill = null)
        assertEquals(null, s.speedKmh)
        assertEquals(SpeedStatus.MISSING, s.speedStatus)
        assertEquals(MotorStatus.UNKNOWN, s.motor)
        assertEquals(KillSwitchStatus.UNKNOWN, s.killSwitch)
        assertEquals(RideState.UNAVAILABLE, s.rideState)
    }

    @Test
    fun `speed is rounded to whole km per hour`() {
        assertEquals(25, map(24.6f, motor = true, kill = false).speedKmh)
        assertEquals(24, map(24.4f, motor = true, kill = false).speedKmh)
    }
}

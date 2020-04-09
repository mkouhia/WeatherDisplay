package fi.mkouhia.weatherdisplay

import org.junit.Test

import org.junit.Assert.*

class MainActivityTest {

    @Test
    fun axisSpec() {
        assertEquals(
            MainActivity.Companion.AxisSpec(0f, 5f, 6, 1f),
            MainActivity.Companion.AxisSpec(0f, 5f, 6)
        )
    }

    @Test
    fun computeLimits() {
        assertEquals(
            MainActivity.Companion.AxisSpec(0.0f, 16.0f, 9),
            MainActivity.computeLimits(0.0f, 15.1f)
        )
    }
    @Test
    fun computeLimits_atUpperLimit() {
        assertEquals(
            MainActivity.Companion.AxisSpec(0.0f, 16.0f, 9),
            MainActivity.computeLimits(0.0f, 16.0f)
        )
    }
    @Test
    fun secondaryLimits() {
        assertEquals(
            MainActivity.Companion.AxisSpec(0.0f, 16.0f, 9),
            MainActivity.computeSecondaryLimits(0f, 15.1f, 9)
        )
    }

}
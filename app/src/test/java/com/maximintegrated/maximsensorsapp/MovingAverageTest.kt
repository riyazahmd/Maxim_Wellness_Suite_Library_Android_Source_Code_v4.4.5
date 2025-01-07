package com.maximintegrated.maximsensorsapp

import org.junit.Assert.assertEquals
import org.junit.Test

class MovingAverageTest {

    @Test
    fun testAddFunctionItemsLessThanBufferSize() {
        val ma = MovingAverage(5)
        ma.add(1)
        ma.add(2)
        ma.add(3)
        ma.add(4)
        assertEquals(2.5f, ma.average())
    }

    @Test
    fun testAddFunctionItemsEqualToBufferSize() {
        val ma = MovingAverage(5)
        ma.add(1)
        ma.add(2)
        ma.add(3)
        ma.add(4)
        ma.add(5)
        assertEquals(3f, ma.average())
    }

    @Test
    fun testAddFunctionItemsGreaterThanBufferSize() {
        val ma = MovingAverage(5)
        ma.add(1)
        ma.add(2)
        ma.add(3)
        ma.add(4)
        ma.add(5)
        ma.add(6)
        assertEquals(4f, ma.average())
    }
}
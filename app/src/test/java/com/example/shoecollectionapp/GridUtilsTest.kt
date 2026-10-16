package com.example.shoecollectionapp

import com.example.shoecollectionapp.utils.GridUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GridUtilsTest {

    @Test
    fun freeCellIsReturnedAsIs() {
        assertEquals(Pair(3, 4), GridUtils.nearestFreeCell(3, 4, emptySet()))
    }

    @Test
    fun occupiedCellMovesToAdjacentOne() {
        val result = GridUtils.nearestFreeCell(3, 4, setOf(Pair(3, 4)))!!
        assertNotEquals(Pair(3, 4), result)
        val dc = result.first - 3
        val dr = result.second - 4
        assertEquals(1, dc * dc + dr * dr)
    }

    @Test
    fun outOfBoundsRequestIsClamped() {
        assertEquals(Pair(24, 0), GridUtils.nearestFreeCell(99, -3, emptySet()))
    }

    @Test
    fun fullGridReturnsNull() {
        val all = buildSet {
            for (c in 0 until GridUtils.COLS) for (r in 0 until GridUtils.ROWS) add(Pair(c, r))
        }
        assertNull(GridUtils.nearestFreeCell(0, 0, all))
    }
}

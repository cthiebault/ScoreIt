package com.sbgapps.scoreit.data.solver

import com.sbgapps.scoreit.data.model.CactusLap
import junit.framework.Assert.assertEquals
import org.junit.Test

class CactusSolverTest {

    private val solver = CactusSolver()

    @Test
    fun `getResults retourne les points bruts du tour`() {
        val points = listOf(10, 20, 30)
        val lap = CactusLap(points)
        assertEquals(points, solver.getResults(lap))
    }

    @Test
    fun `computeScores sans multiple de 50`() {
        val laps = listOf(
            CactusLap(listOf(10, 20, 30)),
            CactusLap(listOf(5, 10, 15))
        )
        assertEquals(listOf(15, 30, 45), solver.computeScores(laps, 3))
    }

    @Test
    fun `computeScores avec score exactement 50 applique -50`() {
        val laps = listOf(
            CactusLap(listOf(30, 20)),
            CactusLap(listOf(20, 30))
        )
        // Player 0: 30 + 20 = 50 -> 50 - 50 = 0
        // Player 1: 20 + 30 = 50 -> 50 - 50 = 0
        assertEquals(listOf(0, 0), solver.computeScores(laps, 2))
    }

    @Test
    fun `computeScores avec score exactement 100 applique -50 une seule fois`() {
        val laps = listOf(
            CactusLap(listOf(40, 10)),
            CactusLap(listOf(10, 40))
        )
        // Player 0: 40 + 10 = 50 -> 0
        // Player 1: 10 + 40 = 50 -> 0
        assertEquals(listOf(0, 0), solver.computeScores(laps, 2))

        val laps2 = listOf(
            CactusLap(listOf(60, 10)),
            CactusLap(listOf(40, 90))
        )
        // Player 0: 60 + 40 = 100 -> 50 (no cascade to 0)
        // Player 1: 10 + 90 = 100 -> 50
        assertEquals(listOf(50, 50), solver.computeScores(laps2, 2))
    }

    @Test
    fun `computeScores avec plusieurs tours et déclenchements -50 accumulés`() {
        val laps = listOf(
            CactusLap(listOf(25, 10)),
            CactusLap(listOf(25, 40))
        )
        // Player 0: 25 + 25 = 50 -> 0
        // Player 1: 10 + 40 = 50 -> 0
        assertEquals(listOf(0, 0), solver.computeScores(laps, 2))

        val laps2 = listOf(
            CactusLap(listOf(25, 10)),
            CactusLap(listOf(25, 10)),  // P0: 50 -> 0, P1: 20
            CactusLap(listOf(25, 10)),  // P0: 25, P1: 30
            CactusLap(listOf(25, 20))   // P0: 50 -> 0, P1: 50 -> 0
        )
        assertEquals(listOf(0, 0), solver.computeScores(laps2, 2))
    }

    @Test
    fun `computeScores score 0 ne déclenche pas la règle`() {
        val laps = listOf(CactusLap(listOf(0, 0)))
        // 0 is a multiple of 50 but should not trigger the rule (score > 0 check)
        assertEquals(listOf(0, 0), solver.computeScores(laps, 2))
    }

    @Test
    fun `computeScores sans tours retourne des zéros`() {
        assertEquals(listOf(0, 0, 0), solver.computeScores(emptyList(), 3))
    }

    @Test
    fun `computeProgressiveScores retourne les scores progressifs corrects`() {
        val laps = listOf(
            CactusLap(listOf(25, 10)),
            CactusLap(listOf(25, 10)),  // P0: 50 -> 0, P1: 20
            CactusLap(listOf(10, 30))   // P0: 10, P1: 50 -> 0
        )
        val progressive = solver.computeProgressiveScores(laps, 2)
        assertEquals(3, progressive.size)
        assertEquals(listOf(25, 10), progressive[0])
        assertEquals(listOf(0, 20), progressive[1])
        assertEquals(listOf(10, 0), progressive[2])
    }
}

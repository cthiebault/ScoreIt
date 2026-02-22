package com.sbgapps.scoreit.data.solver

import com.sbgapps.scoreit.data.model.CactusLap

class CactusSolver {

    fun getResults(lap: CactusLap): List<Int> = lap.points

    fun computeScores(laps: List<CactusLap>, playerCount: Int): List<Int> =
        computeProgressiveScores(laps, playerCount).lastOrNull() ?: List(playerCount) { 0 }

    fun computeProgressiveScores(laps: List<CactusLap>, playerCount: Int): List<List<Int>> {
        val progressive = mutableListOf<List<Int>>()
        val scores = MutableList(playerCount) { 0 }

        for (lap in laps) {
            for (player in 0 until playerCount) {
                scores[player] += lap.points[player]
                if (scores[player] > 0 && scores[player] % 50 == 0) {
                    scores[player] -= 50
                }
            }
            progressive += scores.toList()
        }

        return progressive
    }
}

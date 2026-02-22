/*
 * Copyright 2020 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbgapps.scoreit.app.ui.chart

import androidx.lifecycle.ViewModel
import com.sbgapps.scoreit.app.ui.widget.LinePoint
import com.sbgapps.scoreit.app.ui.widget.LineSet
import com.sbgapps.scoreit.data.interactor.GameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChartViewModel(private val useCase: GameUseCase) : ViewModel() {

    private val innerState = MutableStateFlow<List<LineSet>>(emptyList())
    val lines: StateFlow<List<LineSet>> = innerState

    fun loadLines() {
        if (innerState.value.isEmpty()) {
            innerState.value = getPlayerResults()
        }
    }

    private fun getPlayerResults(): List<LineSet> {
        val lapResults = useCase.getProgressiveScores()

        return useCase.getPlayers().mapIndexed { playerIndex, player ->
            val points = mutableListOf(LinePoint())
            lapResults.forEachIndexed { lapIndex, results ->
                points += LinePoint(lapIndex + 1, results[playerIndex])
            }
            LineSet(points, player.color)
        }
    }
}

package com.sbgapps.scoreit.app.ui.edition.cactus

import com.sbgapps.scoreit.core.ext.replace
import com.sbgapps.scoreit.core.ui.BaseViewModel
import com.sbgapps.scoreit.core.ui.Empty
import com.sbgapps.scoreit.core.ui.State
import com.sbgapps.scoreit.data.interactor.GameUseCase
import com.sbgapps.scoreit.data.model.CactusLap
import com.sbgapps.scoreit.data.model.Player

class CactusEditionViewModel(private val useCase: GameUseCase) : BaseViewModel(Empty) {

    private val editedLap
        get() = useCase.getEditedLap() as CactusLap

    fun loadContent() {
        action {
            setState(getContent())
        }
    }

    fun incrementScore(increment: Int, position: Int) {
        action {
            val oldPoints = editedLap.points
            val newScore = oldPoints[position] + increment
            val newPoints = oldPoints.replace(position, newScore)
            useCase.updateEdition(CactusLap(newPoints))
            setState(getContent())
        }
    }

    fun setScore(position: Int, score: Int) {
        action {
            val newPoints = editedLap.points.replace(position, score)
            useCase.updateEdition(CactusLap(newPoints))
            setState(getContent())
        }
    }

    fun cancelEdition() {
        action {
            useCase.cancelEdition()
            setState(CactusEditionState.Completed)
        }
    }

    fun completeEdition() {
        action {
            useCase.completeEdition()
            setState(CactusEditionState.Completed)
        }
    }

    private fun getContent(): CactusEditionState.Content =
        CactusEditionState.Content(useCase.getPlayers(), editedLap.points)
}

sealed class CactusEditionState : State {
    data class Content(val players: List<Player>, val results: List<Int>) : CactusEditionState()
    data object Completed : CactusEditionState()
}

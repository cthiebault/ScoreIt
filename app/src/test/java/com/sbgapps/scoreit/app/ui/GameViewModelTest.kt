package com.sbgapps.scoreit.app.ui

import com.sbgapps.scoreit.data.interactor.GameUseCase
import com.sbgapps.scoreit.data.model.Player
import com.sbgapps.scoreit.data.model.TarotGame
import com.sbgapps.scoreit.data.model.UniversalGame
import com.sbgapps.scoreit.data.repository.BillingRepo
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {

    private val useCase = mockk<GameUseCase>()
    private val billingRepo = mockk<BillingRepo>()
    private val viewModel = GameViewModel(useCase, billingRepo)

    @Test
    fun `options de nombre de joueurs pour le jeu universel`() {
        val players = (1..5).map { Player("P$it", 0) }
        every { useCase.getGame() } returns UniversalGame(players)
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8), viewModel.getPlayerCountOptions())
    }

    @Test
    fun `options de nombre de joueurs pour le tarot`() {
        val players = (1..5).map { Player("P$it", 0) }
        every { useCase.getGame() } returns TarotGame(players)
        assertEquals(listOf(3, 4, 5), viewModel.getPlayerCountOptions())
    }

    @Test
    fun `menu items pour une partie non commencée`() {
        val players = (1..5).map { Player("P$it", 0) }
        every { useCase.getGame() } returns UniversalGame(players)
        every { useCase.isGameStarted() } returns false
        every { useCase.getSavedFiles() } returns emptyList()

        val items = viewModel.getEnabledMenuItems()
        assertTrue(items.contains(MenuItem.PLAYER_COUNT))
        assertTrue(!items.contains(MenuItem.CHART))
        assertTrue(!items.contains(MenuItem.CLEAR))
    }
}

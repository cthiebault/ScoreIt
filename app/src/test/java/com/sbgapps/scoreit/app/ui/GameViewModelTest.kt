package com.sbgapps.scoreit.app.ui

import com.sbgapps.scoreit.data.interactor.GameUseCase
import com.sbgapps.scoreit.data.model.BeloteGame
import com.sbgapps.scoreit.data.model.CactusGame
import com.sbgapps.scoreit.data.model.CactusLap
import com.sbgapps.scoreit.data.model.CoincheGame
import com.sbgapps.scoreit.data.model.Player
import com.sbgapps.scoreit.data.model.SavedGameInfo
import com.sbgapps.scoreit.data.model.TarotGame
import com.sbgapps.scoreit.data.model.UniversalGame
import com.sbgapps.scoreit.data.model.UniversalLap
import com.sbgapps.scoreit.data.repository.BillingRepo
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class GameViewModelTest {

    private val useCase = mockk<GameUseCase>()
    private val billingRepo = mockk<BillingRepo>()
    private val viewModel = GameViewModel(useCase, billingRepo)

    private val players2 = listOf(Player("Alice", 0xFF0000), Player("Bob", 0x00FF00))
    private val players5 = (1..5).map { Player("P$it", 0) }

    // --- getPlayerCountOptions ---

    @Test
    fun `options de nombre de joueurs pour le jeu universel`() {
        every { useCase.getGame() } returns UniversalGame(players5)
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8), viewModel.getPlayerCountOptions())
    }

    @Test
    fun `options de nombre de joueurs pour le tarot`() {
        every { useCase.getGame() } returns TarotGame(players5)
        assertEquals(listOf(3, 4, 5), viewModel.getPlayerCountOptions())
    }

    @Test
    fun `options de nombre de joueurs pour le cactus`() {
        every { useCase.getGame() } returns CactusGame(players5)
        assertEquals(listOf(2, 3, 4, 5, 6, 7, 8), viewModel.getPlayerCountOptions())
    }

    @Test(expected = IllegalStateException::class)
    fun `options de nombre de joueurs lève une erreur pour la belote`() {
        every { useCase.getGame() } returns BeloteGame(players2)
        viewModel.getPlayerCountOptions()
    }

    @Test(expected = IllegalStateException::class)
    fun `options de nombre de joueurs lève une erreur pour la coinche`() {
        every { useCase.getGame() } returns CoincheGame(players2)
        viewModel.getPlayerCountOptions()
    }

    // --- getEnabledMenuItems ---

    @Test
    fun `menu items pour une partie non commencée`() {
        every { useCase.getGame() } returns UniversalGame(players5)
        every { useCase.isGameStarted() } returns false
        every { useCase.getSavedFiles() } returns emptyList()

        val items = viewModel.getEnabledMenuItems()
        assertTrue(items.contains(MenuItem.PLAYER_COUNT))
        assertFalse(items.contains(MenuItem.CHART))
        assertFalse(items.contains(MenuItem.CLEAR))
        assertFalse(items.contains(MenuItem.SAVED_GAMES))
    }

    @Test
    fun `menu items pour une partie commencée`() {
        every { useCase.getGame() } returns UniversalGame(players5, listOf(UniversalLap(5)))
        every { useCase.isGameStarted() } returns true
        every { useCase.getSavedFiles() } returns emptyList()

        val items = viewModel.getEnabledMenuItems()
        assertTrue(items.contains(MenuItem.PLAYER_COUNT))
        assertTrue(items.contains(MenuItem.CHART))
        assertTrue(items.contains(MenuItem.CLEAR))
    }

    @Test
    fun `menu items avec des parties sauvegardées`() {
        every { useCase.getGame() } returns UniversalGame(players5)
        every { useCase.isGameStarted() } returns false
        every { useCase.getSavedFiles() } returns listOf(SavedGameInfo("f1", "P1 vs P2", 123L))

        val items = viewModel.getEnabledMenuItems()
        assertTrue(items.contains(MenuItem.SAVED_GAMES))
    }

    @Test
    fun `menu items pour la belote sans PLAYER_COUNT`() {
        every { useCase.getGame() } returns BeloteGame(players2)
        every { useCase.isGameStarted() } returns false
        every { useCase.getSavedFiles() } returns emptyList()

        val items = viewModel.getEnabledMenuItems()
        assertFalse(items.contains(MenuItem.PLAYER_COUNT))
    }

    @Test
    fun `menu items pour la coinche sans PLAYER_COUNT`() {
        every { useCase.getGame() } returns CoincheGame(players2)
        every { useCase.isGameStarted() } returns false
        every { useCase.getSavedFiles() } returns emptyList()

        val items = viewModel.getEnabledMenuItems()
        assertFalse(items.contains(MenuItem.PLAYER_COUNT))
    }

    @Test
    fun `menu items pour le cactus avec PLAYER_COUNT`() {
        every { useCase.getGame() } returns CactusGame(players5, listOf(CactusLap(5)))
        every { useCase.isGameStarted() } returns true
        every { useCase.getSavedFiles() } returns emptyList()

        val items = viewModel.getEnabledMenuItems()
        assertTrue(items.contains(MenuItem.PLAYER_COUNT))
        assertTrue(items.contains(MenuItem.CHART))
        assertTrue(items.contains(MenuItem.CLEAR))
    }

    @Test
    fun `menu items pour le tarot avec PLAYER_COUNT`() {
        every { useCase.getGame() } returns TarotGame(players5)
        every { useCase.isGameStarted() } returns false
        every { useCase.getSavedFiles() } returns emptyList()

        val items = viewModel.getEnabledMenuItems()
        assertTrue(items.contains(MenuItem.PLAYER_COUNT))
    }

    // --- canEditPlayer ---

    @Test
    fun `canEditPlayer délègue au useCase`() {
        every { useCase.canEditPlayer(0) } returns true
        every { useCase.canEditPlayer(2) } returns false
        assertTrue(viewModel.canEditPlayer(0))
        assertFalse(viewModel.canEditPlayer(2))
    }
}

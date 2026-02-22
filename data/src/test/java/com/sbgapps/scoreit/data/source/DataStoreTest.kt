package com.sbgapps.scoreit.data.source

import com.sbgapps.scoreit.data.model.GameType
import com.sbgapps.scoreit.data.model.Player
import com.sbgapps.scoreit.data.model.SavedGameInfo
import com.sbgapps.scoreit.data.model.UniversalGame
import com.sbgapps.scoreit.data.repository.CacheRepo
import com.sbgapps.scoreit.data.repository.PreferencesRepo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class DataStoreTest {

    private val cacheRepo = mockk<CacheRepo>(relaxed = true)
    private val prefsRepo = mockk<PreferencesRepo>(relaxed = true)
    private val totalPlayer = Player("Total", 0)
    private val dataStore = DataStore(cacheRepo, prefsRepo, totalPlayer)

    private val players = listOf(Player("Alice", 0xFF0000), Player("Bob", 0x00FF00))
    private val game = UniversalGame(players)

    // --- getGame ---

    @Test
    fun `charge le jeu depuis le cache au premier appel`() {
        every { cacheRepo.loadGame() } returns game
        assertEquals(game, dataStore.getGame())
        verify(exactly = 1) { cacheRepo.loadGame() }
    }

    @Test
    fun `retourne le jeu en cache au deuxième appel`() {
        every { cacheRepo.loadGame() } returns game
        dataStore.getGame()
        dataStore.getGame()
        verify(exactly = 1) { cacheRepo.loadGame() }
    }

    // --- saveGame ---

    @Test
    fun `saveGame met à jour le cache local et persiste`() {
        every { cacheRepo.loadGame() } returns game
        val newGame = UniversalGame(players, listOf())
        dataStore.saveGame(newGame)

        verify { cacheRepo.saveGame(newGame) }
        assertEquals(newGame, dataStore.getGame())
        verify(exactly = 0) { cacheRepo.loadGame() }
    }

    // --- setCurrentGame ---

    @Test
    fun `setCurrentGame invalide le cache`() {
        every { cacheRepo.loadGame() } returns game
        dataStore.getGame()

        dataStore.setCurrentGame(GameType.BELOTE)
        verify { prefsRepo.setGameType(GameType.BELOTE) }

        dataStore.getGame()
        verify(exactly = 2) { cacheRepo.loadGame() }
    }

    // --- setPlayerCount ---

    @Test
    fun `setPlayerCount invalide le cache`() {
        every { cacheRepo.loadGame() } returns game
        dataStore.getGame()

        dataStore.setPlayerCount(4)
        verify { prefsRepo.setPlayerCount(4) }

        dataStore.getGame()
        verify(exactly = 2) { cacheRepo.loadGame() }
    }

    // --- loadGame ---

    @Test
    fun `loadGame par nom met à jour le cache`() {
        val namedGame = UniversalGame(players)
        every { cacheRepo.loadGame("save1") } returns namedGame

        dataStore.loadGame("save1")
        assertEquals(namedGame, dataStore.getGame())
        verify(exactly = 0) { cacheRepo.loadGame(null) }
    }

    // --- removeGame ---

    @Test
    fun `removeGame invalide le cache`() {
        every { cacheRepo.loadGame() } returns game
        dataStore.getGame()

        dataStore.removeGame("file1")
        verify { cacheRepo.removeGame("file1") }

        dataStore.getGame()
        verify(exactly = 2) { cacheRepo.loadGame() }
    }

    // --- renameGame ---

    @Test
    fun `renameGame invalide le cache`() {
        every { cacheRepo.loadGame() } returns game
        dataStore.getGame()

        dataStore.renameGame("old", "new")
        verify { cacheRepo.renameGame("old", "new") }

        dataStore.getGame()
        verify(exactly = 2) { cacheRepo.loadGame() }
    }

    // --- Preferences delegation ---

    @Test
    fun `isUniversalTotalDisplayed délègue aux préférences`() {
        every { prefsRepo.isTotalDisplayed(GameType.UNIVERSAL) } returns true
        assertTrue(dataStore.isUniversalTotalDisplayed())
    }

    @Test
    fun `setUniversalTotalDisplayed délègue aux préférences`() {
        dataStore.setUniversalTotalDisplayed(true)
        verify { prefsRepo.setTotalDisplayed(GameType.UNIVERSAL, true) }
    }

    @Test
    fun `isBeloteScoreRounded délègue aux préférences`() {
        every { prefsRepo.isRounded(GameType.BELOTE) } returns false
        assertFalse(dataStore.isBeloteScoreRounded())
    }

    @Test
    fun `isCoincheScoreRounded délègue aux préférences`() {
        every { prefsRepo.isRounded(GameType.COINCHE) } returns true
        assertTrue(dataStore.isCoincheScoreRounded())
    }

    @Test
    fun `getPrefThemeMode délègue aux préférences`() {
        every { prefsRepo.getThemeMode() } returns "dark"
        assertEquals("dark", dataStore.getPrefThemeMode())
    }

    @Test
    fun `setPrefThemeMode délègue aux préférences`() {
        dataStore.setPrefThemeMode("dark")
        verify { prefsRepo.setThemeMode("dark") }
    }

    @Test
    fun `getSavedFiles délègue au cacheRepo`() {
        val files = listOf(SavedGameInfo("f1", "P1 vs P2", 123L))
        every { cacheRepo.getSavedGames() } returns files
        assertEquals(files, dataStore.getSavedFiles())
    }

    @Test
    fun `createGame délègue au cacheRepo et met à jour le cache`() {
        every { cacheRepo.loadGame() } returns game
        val createdGame = UniversalGame(players)
        every { cacheRepo.createGame(game, "new") } returns createdGame

        dataStore.getGame()
        dataStore.createGame("new")
        assertEquals(createdGame, dataStore.getGame())
    }
}

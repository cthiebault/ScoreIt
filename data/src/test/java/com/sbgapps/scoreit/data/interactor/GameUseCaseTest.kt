package com.sbgapps.scoreit.data.interactor

import com.sbgapps.scoreit.data.model.BeloteGame
import com.sbgapps.scoreit.data.model.BeloteLap
import com.sbgapps.scoreit.data.model.CactusGame
import com.sbgapps.scoreit.data.model.CactusLap
import com.sbgapps.scoreit.data.model.CoincheGame
import com.sbgapps.scoreit.data.model.CoincheLap
import com.sbgapps.scoreit.data.model.GameType
import com.sbgapps.scoreit.data.model.Player
import com.sbgapps.scoreit.data.model.PlayerPosition
import com.sbgapps.scoreit.data.model.SavedGameInfo
import com.sbgapps.scoreit.data.model.TarotGame
import com.sbgapps.scoreit.data.model.TarotLap
import com.sbgapps.scoreit.data.model.UniversalGame
import com.sbgapps.scoreit.data.model.UniversalLap
import com.sbgapps.scoreit.data.solver.BeloteSolver
import com.sbgapps.scoreit.data.solver.CactusSolver
import com.sbgapps.scoreit.data.solver.CoincheSolver
import com.sbgapps.scoreit.data.solver.TarotSolver
import com.sbgapps.scoreit.data.solver.UniversalSolver
import com.sbgapps.scoreit.data.source.DataStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameUseCaseTest {

    private val dataStore = mockk<DataStore>(relaxed = true)
    private val universalSolver = mockk<UniversalSolver>()
    private val tarotSolver = mockk<TarotSolver>()
    private val beloteSolver = mockk<BeloteSolver>()
    private val coincheSolver = mockk<CoincheSolver>()
    private val cactusSolver = mockk<CactusSolver>()
    private val useCase = GameUseCase(dataStore, universalSolver, tarotSolver, beloteSolver, coincheSolver, cactusSolver)

    private val players2 = listOf(Player("Alice", 0xFF0000), Player("Bob", 0x00FF00))
    private val players3 = listOf(Player("Alice", 0xFF0000), Player("Bob", 0x00FF00), Player("Charlie", 0x0000FF))
    private val totalPlayer = Player("Total", 0)

    // --- getGame ---

    @Test
    fun `retourne le jeu depuis le dataStore`() {
        val game = UniversalGame(players2)
        every { dataStore.getGame() } returns game
        assertEquals(game, useCase.getGame())
    }

    // --- getPlayers ---

    @Test
    fun `retourne les joueurs sans total pour un jeu universel`() {
        every { dataStore.getGame() } returns UniversalGame(players3)
        assertEquals(players3, useCase.getPlayers())
    }

    @Test
    fun `retourne les joueurs avec total quand activé pour un jeu universel`() {
        every { dataStore.getGame() } returns UniversalGame(players3)
        every { dataStore.isUniversalTotalDisplayed() } returns true
        every { dataStore.totalPlayer } returns totalPlayer

        val result = useCase.getPlayers(withTotal = true)
        assertEquals(4, result.size)
        assertEquals(totalPlayer, result.last())
    }

    @Test
    fun `retourne les joueurs sans total quand désactivé pour un jeu universel`() {
        every { dataStore.getGame() } returns UniversalGame(players3)
        every { dataStore.isUniversalTotalDisplayed() } returns false

        val result = useCase.getPlayers(withTotal = true)
        assertEquals(3, result.size)
    }

    @Test
    fun `retourne les joueurs sans total pour un jeu belote même si demandé`() {
        every { dataStore.getGame() } returns BeloteGame(players2)
        assertEquals(players2, useCase.getPlayers(withTotal = true))
    }

    // --- getResults ---

    @Test
    fun `délègue getResults au solver universel`() {
        val lap = UniversalLap(listOf(10, 20, 30))
        every { dataStore.isUniversalTotalDisplayed() } returns false
        every { universalSolver.getResults(lap, false) } returns listOf(10, 20, 30)
        assertEquals(listOf(10, 20, 30), useCase.getResults(lap))
    }

    @Test
    fun `délègue getResults au solver tarot`() {
        val lap = TarotLap(3)
        every { tarotSolver.getResults(lap) } returns listOf(50, -25, -25)
        assertEquals(listOf(50, -25, -25), useCase.getResults(lap))
    }

    @Test
    fun `délègue getResults au solver belote`() {
        val lap = BeloteLap()
        every { beloteSolver.getResults(lap) } returns (listOf(90, 72) to true)
        assertEquals(listOf(90, 72), useCase.getResults(lap))
    }

    @Test
    fun `délègue getResults au solver coinche`() {
        val lap = CoincheLap()
        every { coincheSolver.getResults(lap) } returns (listOf(200, 0) to true)
        assertEquals(listOf(200, 0), useCase.getResults(lap))
    }

    @Test
    fun `délègue getResults au solver cactus`() {
        val lap = CactusLap(listOf(10, 20))
        every { cactusSolver.getResults(lap) } returns listOf(10, 20)
        assertEquals(listOf(10, 20), useCase.getResults(lap))
    }

    // --- getDisplayResults ---

    @Test
    fun `délègue getDisplayResults au solver tarot`() {
        val lap = TarotLap(3)
        val expected = listOf("50", "-25", "-25") to true
        every { tarotSolver.getDisplayResults(lap) } returns expected
        assertEquals(expected, useCase.getDisplayResults(lap))
    }

    @Test
    fun `délègue getDisplayResults au solver belote`() {
        val lap = BeloteLap()
        val expected = listOf("90", "72") to true
        every { beloteSolver.getDisplayResults(lap) } returns expected
        assertEquals(expected, useCase.getDisplayResults(lap))
    }

    @Test
    fun `délègue getDisplayResults au solver coinche`() {
        val lap = CoincheLap()
        val expected = listOf("200", "0") to true
        every { coincheSolver.getDisplayResults(lap) } returns expected
        assertEquals(expected, useCase.getDisplayResults(lap))
    }

    @Test
    fun `getDisplayResults lève une erreur pour un jeu universel`() {
        assertThrows<IllegalStateException> { useCase.getDisplayResults(UniversalLap(listOf(10, 20))) }
    }

    @Test
    fun `getDisplayResults lève une erreur pour un jeu cactus`() {
        assertThrows<IllegalStateException> { useCase.getDisplayResults(CactusLap(listOf(10, 20))) }
    }

    // --- getScores ---

    @Test
    fun `délègue getScores au solver universel`() {
        val game = UniversalGame(players3, listOf(UniversalLap(listOf(10, 20, 30))))
        every { dataStore.getGame() } returns game
        every { dataStore.isUniversalTotalDisplayed() } returns false
        every { universalSolver.computeScores(game.laps, 3, false) } returns listOf(10, 20, 30)
        assertEquals(listOf(10, 20, 30), useCase.getScores())
    }

    @Test
    fun `délègue getScores au solver belote`() {
        val laps = listOf(BeloteLap())
        every { dataStore.getGame() } returns BeloteGame(players2, laps)
        every { beloteSolver.computeScores(laps) } returns listOf(90, 72)
        assertEquals(listOf(90, 72), useCase.getScores())
    }

    @Test
    fun `délègue getScores au solver coinche`() {
        val laps = listOf(CoincheLap())
        every { dataStore.getGame() } returns CoincheGame(players2, laps)
        every { coincheSolver.computeScores(laps) } returns listOf(200, 0)
        assertEquals(listOf(200, 0), useCase.getScores())
    }

    @Test
    fun `délègue getScores au solver tarot`() {
        val laps = listOf(TarotLap(3))
        every { dataStore.getGame() } returns TarotGame(players3, laps)
        every { tarotSolver.computeScores(laps, 3) } returns listOf(50, -25, -25)
        assertEquals(listOf(50, -25, -25), useCase.getScores())
    }

    @Test
    fun `délègue getScores au solver cactus`() {
        val laps = listOf(CactusLap(listOf(10, 20)))
        every { dataStore.getGame() } returns CactusGame(players2, laps)
        every { cactusSolver.computeScores(laps, 2) } returns listOf(10, 20)
        assertEquals(listOf(10, 20), useCase.getScores())
    }

    // --- isGameStarted ---

    @Test
    fun `partie non commencée sans tours`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        assertFalse(useCase.isGameStarted())
    }

    @Test
    fun `partie commencée avec des tours`() {
        every { dataStore.getGame() } returns UniversalGame(players2, listOf(UniversalLap(2)))
        assertTrue(useCase.isGameStarted())
    }

    // --- Edition state machine ---

    @Test
    fun `getEditedLap crée un tour universel quand pas d'état`() {
        every { dataStore.getGame() } returns UniversalGame(players3)
        val lap = useCase.getEditedLap()
        assertTrue(lap is UniversalLap)
        assertEquals(listOf(0, 0, 0), (lap as UniversalLap).points)
    }

    @Test
    fun `getEditedLap crée un tour tarot quand pas d'état`() {
        every { dataStore.getGame() } returns TarotGame(players3)
        val lap = useCase.getEditedLap()
        assertTrue(lap is TarotLap)
        assertEquals(3, (lap as TarotLap).playerCount)
    }

    @Test
    fun `getEditedLap crée un tour belote quand pas d'état`() {
        every { dataStore.getGame() } returns BeloteGame(players2)
        val lap = useCase.getEditedLap()
        assertTrue(lap is BeloteLap)
    }

    @Test
    fun `getEditedLap crée un tour coinche quand pas d'état`() {
        every { dataStore.getGame() } returns CoincheGame(players2)
        val lap = useCase.getEditedLap()
        assertTrue(lap is CoincheLap)
    }

    @Test
    fun `getEditedLap crée un tour cactus quand pas d'état`() {
        every { dataStore.getGame() } returns CactusGame(players3)
        val lap = useCase.getEditedLap()
        assertTrue(lap is CactusLap)
        assertEquals(listOf(0, 0, 0), (lap as CactusLap).points)
    }

    @Test
    fun `getEditedLap retourne le même tour en mode création`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        val first = useCase.getEditedLap()
        val second = useCase.getEditedLap()
        assertTrue(first === second)
    }

    @Test
    fun `updateEdition met à jour le tour en mode création`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        useCase.getEditedLap()

        val updated = UniversalLap(listOf(10, 20))
        useCase.updateEdition(updated)
        assertEquals(updated, useCase.getEditedLap())
    }

    @Test
    fun `updateEdition met à jour le tour en mode modification`() {
        val lap = UniversalLap(listOf(5, 10))
        every { dataStore.getGame() } returns UniversalGame(players2, listOf(lap))

        useCase.modifyLap(0)
        val modified = UniversalLap(listOf(15, 25))
        useCase.updateEdition(modified)
        assertEquals(modified, useCase.getEditedLap())
    }

    @Test
    fun `completeEdition ajoute un tour en mode création`() {
        val game = UniversalGame(players2)
        every { dataStore.getGame() } returns game

        useCase.getEditedLap()
        val newLap = UniversalLap(listOf(10, 20))
        useCase.updateEdition(newLap)

        val gameSlot = slot<UniversalGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.completeEdition()
        assertEquals(1, gameSlot.captured.laps.size)
        assertEquals(newLap, gameSlot.captured.laps[0])
    }

    @Test
    fun `completeEdition remplace un tour en mode modification`() {
        val originalLap = UniversalLap(listOf(5, 10))
        val game = UniversalGame(players2, listOf(originalLap))
        every { dataStore.getGame() } returns game

        useCase.modifyLap(0)
        val modifiedLap = UniversalLap(listOf(15, 25))
        useCase.updateEdition(modifiedLap)

        val gameSlot = slot<UniversalGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.completeEdition()
        assertEquals(1, gameSlot.captured.laps.size)
        assertEquals(modifiedLap, gameSlot.captured.laps[0])
    }

    @Test
    fun `completeEdition réinitialise l'état d'édition`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        useCase.getEditedLap()
        useCase.completeEdition()

        // Après completeEdition, un nouvel appel à getEditedLap crée un nouveau tour
        val newLap = useCase.getEditedLap()
        assertTrue(newLap is UniversalLap)
    }

    @Test
    fun `cancelEdition réinitialise l'état en mode modification`() {
        val lap = UniversalLap(listOf(5, 10))
        every { dataStore.getGame() } returns UniversalGame(players2, listOf(lap))

        useCase.modifyLap(0)
        useCase.cancelEdition()

        // Après cancelEdition, un nouvel appel à getEditedLap crée un nouveau tour
        val newLap = useCase.getEditedLap()
        assertEquals(UniversalLap(listOf(0, 0)), newLap)
    }

    @Test
    fun `cancelEdition ne réinitialise pas l'état en mode création`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        val firstLap = useCase.getEditedLap()
        useCase.cancelEdition()

        val secondLap = useCase.getEditedLap()
        assertTrue(firstLap === secondLap)
    }

    // --- Player management ---

    @Test
    fun `editPlayerName reconstruit le jeu avec le nouveau nom`() {
        val game = UniversalGame(players2)
        every { dataStore.getGame() } returns game
        every { dataStore.isUniversalTotalDisplayed() } returns false

        val gameSlot = slot<UniversalGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.editPlayerName(0, "Zoe")
        assertEquals("Zoe", gameSlot.captured.players[0].name)
        assertEquals(players2[0].color, gameSlot.captured.players[0].color)
        assertEquals(players2[1], gameSlot.captured.players[1])
    }

    @Test
    fun `editPlayerColor reconstruit le jeu avec la nouvelle couleur`() {
        val game = UniversalGame(players2)
        every { dataStore.getGame() } returns game
        every { dataStore.isUniversalTotalDisplayed() } returns false

        val gameSlot = slot<UniversalGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.editPlayerColor(0, 0xABCDEF)
        assertEquals(players2[0].name, gameSlot.captured.players[0].name)
        assertEquals(0xABCDEF, gameSlot.captured.players[0].color)
    }

    @Test
    fun `editPlayerName fonctionne pour un jeu belote`() {
        val game = BeloteGame(players2)
        every { dataStore.getGame() } returns game

        val gameSlot = slot<BeloteGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.editPlayerName(1, "Zoe")
        assertEquals("Zoe", gameSlot.captured.players[1].name)
    }

    @Test
    fun `editPlayerName fonctionne pour un jeu cactus`() {
        val game = CactusGame(players3)
        every { dataStore.getGame() } returns game

        val gameSlot = slot<CactusGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.editPlayerName(2, "Zoe")
        assertEquals("Zoe", gameSlot.captured.players[2].name)
    }

    // --- canEditPlayer ---

    @Test
    fun `canEditPlayer retourne true pour un jeu universel sans total`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        every { dataStore.isUniversalTotalDisplayed() } returns false
        assertTrue(useCase.canEditPlayer(0))
        assertTrue(useCase.canEditPlayer(1))
    }

    @Test
    fun `canEditPlayer bloque la position total pour un jeu universel avec total`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        every { dataStore.isUniversalTotalDisplayed() } returns true
        assertTrue(useCase.canEditPlayer(0))
        assertTrue(useCase.canEditPlayer(1))
        assertFalse(useCase.canEditPlayer(2))
    }

    @Test
    fun `canEditPlayer retourne toujours true pour les autres jeux`() {
        every { dataStore.getGame() } returns BeloteGame(players2)
        assertTrue(useCase.canEditPlayer(0))
        assertTrue(useCase.canEditPlayer(1))
    }

    // --- Lap CRUD ---

    @Test
    fun `deleteLap supprime le tour à la position donnée`() {
        val laps = listOf(UniversalLap(listOf(10, 20)), UniversalLap(listOf(30, 40)))
        every { dataStore.getGame() } returns UniversalGame(players2, laps)

        val gameSlot = slot<UniversalGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.deleteLap(0)
        assertEquals(1, gameSlot.captured.laps.size)
        assertEquals(listOf(30, 40), gameSlot.captured.laps[0].points)
    }

    @Test
    fun `deleteLap fonctionne pour un jeu belote`() {
        val laps = listOf(BeloteLap(), BeloteLap(taker = PlayerPosition.TWO))
        every { dataStore.getGame() } returns BeloteGame(players2, laps)

        val gameSlot = slot<BeloteGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.deleteLap(1)
        assertEquals(1, gameSlot.captured.laps.size)
    }

    @Test
    fun `reset supprime tous les tours et sauvegarde`() {
        val laps = listOf(UniversalLap(listOf(10, 20)), UniversalLap(listOf(30, 40)))
        every { dataStore.getGame() } returns UniversalGame(players2, laps)

        val gameSlot = slot<UniversalGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.reset()
        assertTrue(gameSlot.captured.laps.isEmpty())
        assertEquals(players2, gameSlot.captured.players)
    }

    @Test
    fun `reset fonctionne pour un jeu cactus`() {
        every { dataStore.getGame() } returns CactusGame(players3, listOf(CactusLap(3)))

        val gameSlot = slot<CactusGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.reset()
        assertTrue(gameSlot.captured.laps.isEmpty())
        assertEquals(players3, gameSlot.captured.players)
    }

    // --- Markers ---

    @Test
    fun `getMarkers indique le premier joueur pour un jeu universel sans tours`() {
        every { dataStore.getGame() } returns UniversalGame(players3)
        every { dataStore.isUniversalTotalDisplayed() } returns false
        assertEquals(listOf(true, false, false), useCase.getMarkers())
    }

    @Test
    fun `getMarkers indique le deuxième joueur après un tour universel`() {
        every { dataStore.getGame() } returns UniversalGame(players3, listOf(UniversalLap(3)))
        every { dataStore.isUniversalTotalDisplayed() } returns false
        assertEquals(listOf(false, true, false), useCase.getMarkers())
    }

    @Test
    fun `getMarkers ajoute false pour le total dans un jeu universel avec total`() {
        every { dataStore.getGame() } returns UniversalGame(players3)
        every { dataStore.isUniversalTotalDisplayed() } returns true
        every { dataStore.totalPlayer } returns totalPlayer
        assertEquals(listOf(true, false, false, false), useCase.getMarkers())
    }

    @Test
    fun `getMarkers retourne tout false pour un jeu belote`() {
        every { dataStore.getGame() } returns BeloteGame(players2)
        assertEquals(listOf(false, false), useCase.getMarkers())
    }

    @Test
    fun `getMarkers retourne tout false pour un jeu coinche`() {
        every { dataStore.getGame() } returns CoincheGame(players2)
        assertEquals(listOf(false, false), useCase.getMarkers())
    }

    @Test
    fun `getMarkers fonctionne pour un jeu cactus`() {
        every { dataStore.getGame() } returns CactusGame(players3, listOf(CactusLap(3)))
        assertEquals(listOf(false, true, false), useCase.getMarkers())
    }

    // --- Progressive scores ---

    @Test
    fun `getProgressiveScores cumule les scores pour un jeu universel`() {
        val laps = listOf(UniversalLap(listOf(10, 20)), UniversalLap(listOf(5, 15)))
        every { dataStore.getGame() } returns UniversalGame(players2, laps)
        every { dataStore.isUniversalTotalDisplayed() } returns false
        every { universalSolver.getResults(laps[0], false) } returns listOf(10, 20)
        every { universalSolver.getResults(laps[1], false) } returns listOf(5, 15)

        val result = useCase.getProgressiveScores()
        assertEquals(listOf(listOf(10, 20), listOf(15, 35)), result)
    }

    @Test
    fun `getProgressiveScores retourne une liste vide sans tours`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        assertTrue(useCase.getProgressiveScores().isEmpty())
    }

    @Test
    fun `getProgressiveScores délègue au solver cactus`() {
        val laps = listOf(CactusLap(listOf(10, 20)))
        every { dataStore.getGame() } returns CactusGame(players2, laps)
        val expected = listOf(listOf(10, 20))
        every { cactusSolver.computeProgressiveScores(laps, 2) } returns expected
        assertEquals(expected, useCase.getProgressiveScores())
    }

    // --- Delegation ---

    @Test
    fun `setGameType délègue au dataStore et réinitialise l'état`() {
        every { dataStore.getGame() } returns UniversalGame(players2)
        useCase.getEditedLap()

        useCase.setGameType(GameType.BELOTE)
        verify { dataStore.setCurrentGame(GameType.BELOTE) }

        // L'état d'édition est réinitialisé
        every { dataStore.getGame() } returns BeloteGame(players2)
        val lap = useCase.getEditedLap()
        assertTrue(lap is BeloteLap)
    }

    @Test
    fun `setPlayerCount délègue au dataStore`() {
        useCase.setPlayerCount(4)
        verify { dataStore.setPlayerCount(4) }
    }

    @Test
    fun `createGame délègue au dataStore`() {
        useCase.createGame("test")
        verify { dataStore.createGame("test") }
    }

    @Test
    fun `loadGame délègue au dataStore`() {
        useCase.loadGame("test")
        verify { dataStore.loadGame("test") }
    }

    @Test
    fun `removeGame délègue au dataStore`() {
        useCase.removeGame("test")
        verify { dataStore.removeGame("test") }
    }

    @Test
    fun `renameGame délègue au dataStore`() {
        useCase.renameGame("old", "new")
        verify { dataStore.renameGame("old", "new") }
    }

    @Test
    fun `getSavedFiles délègue au dataStore`() {
        val files = listOf(SavedGameInfo("file1", "P1 vs P2", 123L))
        every { dataStore.getSavedFiles() } returns files
        assertEquals(files, useCase.getSavedFiles())
    }

    // --- modifyLap ---

    @Test
    fun `modifyLap met l'état en modification`() {
        val lap = UniversalLap(listOf(10, 20))
        every { dataStore.getGame() } returns UniversalGame(players2, listOf(lap))

        useCase.modifyLap(0)
        assertEquals(lap, useCase.getEditedLap())
    }

    // --- completeEdition pour d'autres types de jeu ---

    @Test
    fun `completeEdition ajoute un tour belote`() {
        every { dataStore.getGame() } returns BeloteGame(players2)
        useCase.getEditedLap()

        val gameSlot = slot<BeloteGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.completeEdition()
        assertEquals(1, gameSlot.captured.laps.size)
    }

    @Test
    fun `completeEdition ajoute un tour cactus`() {
        every { dataStore.getGame() } returns CactusGame(players3)
        useCase.getEditedLap()

        val gameSlot = slot<CactusGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.completeEdition()
        assertEquals(1, gameSlot.captured.laps.size)
    }

    @Test
    fun `completeEdition lève une erreur sans état d'édition`() {
        assertThrows<IllegalStateException> { useCase.completeEdition() }
    }

    // --- deleteLap pour d'autres types ---

    @Test
    fun `deleteLap fonctionne pour un jeu cactus`() {
        val laps = listOf(CactusLap(listOf(10, 20)), CactusLap(listOf(30, 40)))
        every { dataStore.getGame() } returns CactusGame(players2, laps)

        val gameSlot = slot<CactusGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.deleteLap(0)
        assertEquals(1, gameSlot.captured.laps.size)
        assertEquals(listOf(30, 40), gameSlot.captured.laps[0].points)
    }

    @Test
    fun `deleteLap fonctionne pour un jeu tarot`() {
        val laps = listOf(TarotLap(3), TarotLap(3, taker = PlayerPosition.TWO))
        every { dataStore.getGame() } returns TarotGame(players3, laps)

        val gameSlot = slot<TarotGame>()
        every { dataStore.saveGame(capture(gameSlot)) } returns Unit

        useCase.deleteLap(0)
        assertEquals(1, gameSlot.captured.laps.size)
        assertEquals(PlayerPosition.TWO, gameSlot.captured.laps[0].taker)
    }
}

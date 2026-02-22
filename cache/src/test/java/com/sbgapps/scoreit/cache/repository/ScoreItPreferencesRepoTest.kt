package com.sbgapps.scoreit.cache.repository

import android.content.SharedPreferences
import com.sbgapps.scoreit.data.model.GameType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class ScoreItPreferencesRepoTest {

    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val preferences = mockk<SharedPreferences>(relaxed = true)
    private val repo = ScoreItPreferencesRepo(preferences)

    init {
        every { preferences.edit() } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
    }

    // --- getPlayerCount ---

    @Test
    fun `retourne 2 joueurs pour la belote`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.BELOTE.ordinal
        assertEquals(2, repo.getPlayerCount())
    }

    @Test
    fun `retourne 2 joueurs pour la coinche`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.COINCHE.ordinal
        assertEquals(2, repo.getPlayerCount())
    }

    @Test
    fun `retourne le nombre de joueurs stocké pour l'universel`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.UNIVERSAL.ordinal
        every { preferences.getInt("universal_player_count", 5) } returns 3
        assertEquals(3, repo.getPlayerCount())
    }

    @Test
    fun `retourne le nombre de joueurs stocké pour le tarot`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.TAROT.ordinal
        every { preferences.getInt("tarot_player_count", 5) } returns 4
        assertEquals(4, repo.getPlayerCount())
    }

    @Test
    fun `retourne le nombre de joueurs stocké pour le cactus`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.CACTUS.ordinal
        every { preferences.getInt("cactus_player_count", 5) } returns 6
        assertEquals(6, repo.getPlayerCount())
    }

    // --- setPlayerCount ---

    @Test
    fun `setPlayerCount pour l'universel avec minimum 2`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.UNIVERSAL.ordinal
        repo.setPlayerCount(1)
        verify { editor.putInt("universal_player_count", 2) }
    }

    @Test
    fun `setPlayerCount pour l'universel sans clampage au-dessus du minimum`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.UNIVERSAL.ordinal
        repo.setPlayerCount(5)
        verify { editor.putInt("universal_player_count", 5) }
    }

    @Test
    fun `setPlayerCount pour le tarot avec minimum 3`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.TAROT.ordinal
        repo.setPlayerCount(2)
        verify { editor.putInt("tarot_player_count", 3) }
    }

    @Test
    fun `setPlayerCount pour le tarot sans clampage au-dessus du minimum`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.TAROT.ordinal
        repo.setPlayerCount(5)
        verify { editor.putInt("tarot_player_count", 5) }
    }

    @Test
    fun `setPlayerCount pour le cactus avec minimum 2`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.CACTUS.ordinal
        repo.setPlayerCount(1)
        verify { editor.putInt("cactus_player_count", 2) }
    }

    @Test
    fun `setPlayerCount pour le cactus sans clampage au-dessus du minimum`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.CACTUS.ordinal
        repo.setPlayerCount(7)
        verify { editor.putInt("cactus_player_count", 7) }
    }

    @Test(expected = IllegalStateException::class)
    fun `setPlayerCount lève une erreur pour la belote`() {
        every { preferences.getInt("selected_game", any()) } returns GameType.BELOTE.ordinal
        repo.setPlayerCount(3)
    }

    // --- isRounded ---

    @Test
    fun `isRounded retourne true par défaut pour la belote`() {
        every { preferences.getBoolean("belote_round_score", true) } returns true
        assertTrue(repo.isRounded(GameType.BELOTE))
    }

    @Test
    fun `isRounded retourne la valeur stockée pour la coinche`() {
        every { preferences.getBoolean("coinche_round_score", true) } returns false
        assertFalse(repo.isRounded(GameType.COINCHE))
    }

    @Test(expected = IllegalStateException::class)
    fun `isRounded lève une erreur pour l'universel`() {
        repo.isRounded(GameType.UNIVERSAL)
    }

    // --- setRounded ---

    @Test
    fun `setRounded pour la belote`() {
        repo.setRounded(GameType.BELOTE, false)
        verify { editor.putBoolean("belote_round_score", false) }
    }

    @Test
    fun `setRounded pour la coinche`() {
        repo.setRounded(GameType.COINCHE, true)
        verify { editor.putBoolean("coinche_round_score", true) }
    }

    @Test(expected = IllegalStateException::class)
    fun `setRounded lève une erreur pour le tarot`() {
        repo.setRounded(GameType.TAROT, true)
    }

    // --- isTotalDisplayed ---

    @Test
    fun `isTotalDisplayed retourne false par défaut pour l'universel`() {
        every { preferences.getBoolean("universal_show_total", false) } returns false
        assertFalse(repo.isTotalDisplayed(GameType.UNIVERSAL))
    }

    @Test
    fun `isTotalDisplayed retourne la valeur stockée pour l'universel`() {
        every { preferences.getBoolean("universal_show_total", false) } returns true
        assertTrue(repo.isTotalDisplayed(GameType.UNIVERSAL))
    }

    @Test(expected = IllegalStateException::class)
    fun `isTotalDisplayed lève une erreur pour la belote`() {
        repo.isTotalDisplayed(GameType.BELOTE)
    }

    // --- setTotalDisplayed ---

    @Test
    fun `setTotalDisplayed pour l'universel`() {
        repo.setTotalDisplayed(GameType.UNIVERSAL, true)
        verify { editor.putBoolean("universal_show_total", true) }
    }

    @Test(expected = IllegalStateException::class)
    fun `setTotalDisplayed lève une erreur pour le cactus`() {
        repo.setTotalDisplayed(GameType.CACTUS, true)
    }

    // --- Theme ---

    @Test
    fun `retourne le thème auto par défaut`() {
        every { preferences.getString("preferred_theme", null) } returns null
        assertEquals("THEME_MODE_AUTO", repo.getThemeMode())
    }

    @Test
    fun `retourne le thème stocké`() {
        every { preferences.getString("preferred_theme", null) } returns "dark"
        assertEquals("dark", repo.getThemeMode())
    }

    @Test
    fun `setThemeMode stocke la valeur`() {
        repo.setThemeMode("dark")
        verify { editor.putString("preferred_theme", "dark") }
    }

    // --- GameType ---

    @Test
    fun `retourne le type de jeu universel par défaut`() {
        every { preferences.getInt("selected_game", GameType.UNIVERSAL.ordinal) } returns GameType.UNIVERSAL.ordinal
        assertEquals(GameType.UNIVERSAL, repo.getGameType())
    }

    @Test
    fun `setGameType stocke la valeur`() {
        repo.setGameType(GameType.CACTUS)
        verify { editor.putInt("selected_game", GameType.CACTUS.ordinal) }
    }
}

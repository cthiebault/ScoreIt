package com.sbgapps.scoreit.cache.repository

import android.content.SharedPreferences
import com.sbgapps.scoreit.data.model.GameType
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import org.junit.Test

class ScoreItPreferencesRepoTest {

    private val preferences = mockk<SharedPreferences>(relaxed = true)
    private val repo = ScoreItPreferencesRepo(preferences)

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
    fun `retourne le thème auto par défaut`() {
        every { preferences.getString("preferred_theme", null) } returns null
        assertEquals("THEME_MODE_AUTO", repo.getThemeMode())
    }

    @Test
    fun `retourne le thème stocké`() {
        every { preferences.getString("preferred_theme", null) } returns "dark"
        assertEquals("dark", repo.getThemeMode())
    }
}

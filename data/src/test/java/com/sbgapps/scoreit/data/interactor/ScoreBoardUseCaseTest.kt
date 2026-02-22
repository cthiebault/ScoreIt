package com.sbgapps.scoreit.data.interactor

import com.sbgapps.scoreit.data.model.ScoreBoard
import com.sbgapps.scoreit.data.repository.CacheRepo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ScoreBoardUseCaseTest {

    private val cacheRepo = mockk<CacheRepo>(relaxed = true)
    private val useCase = ScoreBoardUseCase(cacheRepo)

    private val scoreBoard = ScoreBoard(scoreOne = 10, nameOne = "Alice", scoreTwo = 20, nameTwo = "Bob")

    @Test
    fun `getScoreBoard charge et met en cache`() {
        every { cacheRepo.loadScoreBoard() } returns scoreBoard
        assertEquals(scoreBoard, useCase.getScoreBoard())

        useCase.getScoreBoard()
        verify(exactly = 1) { cacheRepo.loadScoreBoard() }
    }

    @Test
    fun `saveScoreBoard persiste et met à jour le cache`() {
        val updated = scoreBoard.copy(scoreOne = 30)
        useCase.saveScoreBoard(updated)

        verify { cacheRepo.saveScoreBoard(updated) }
        assertEquals(updated, useCase.getScoreBoard())
        verify(exactly = 0) { cacheRepo.loadScoreBoard() }
    }

    @Test
    fun `reset remet les scores à zéro et préserve les noms`() {
        every { cacheRepo.loadScoreBoard() } returns scoreBoard
        val result = useCase.reset()

        assertEquals(0, result.scoreOne)
        assertEquals(0, result.scoreTwo)
        assertEquals("Alice", result.nameOne)
        assertEquals("Bob", result.nameTwo)
    }
}

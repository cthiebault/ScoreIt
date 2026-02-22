package com.sbgapps.scoreit.app.ui.edition

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class EditionComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `PointsStepper displays label and points`() {
        composeTestRule.setContent {
            PointsStepper(
                label = "Score",
                pointsText = "42",
                stepByTen = Step(canAdd = true, canSubtract = true),
                stepByOne = Step(canAdd = true, canSubtract = true),
                onIncrement = {}
            )
        }

        composeTestRule.onNodeWithText("Score").assertExists()
        composeTestRule.onNodeWithText("42").assertExists()
    }

    @Test
    fun `PointsStepper calls onIncrement with correct value`() {
        var lastIncrement = 0
        composeTestRule.setContent {
            PointsStepper(
                label = "Score",
                pointsText = "0",
                stepByTen = Step(canAdd = true, canSubtract = true),
                stepByOne = Step(canAdd = true, canSubtract = true),
                onIncrement = { lastIncrement = it }
            )
        }

        composeTestRule.onNodeWithText("+1").performClick()
        assertEquals(1, lastIncrement)

        composeTestRule.onNodeWithText("-1").performClick()
        assertEquals(-1, lastIncrement)

        composeTestRule.onNodeWithText("+10").performClick()
        assertEquals(10, lastIncrement)

        composeTestRule.onNodeWithText("-10").performClick()
        assertEquals(-10, lastIncrement)
    }

    @Test
    fun `PointsStepper disables buttons based on Step state`() {
        composeTestRule.setContent {
            PointsStepper(
                label = "Score",
                pointsText = "0",
                stepByTen = Step(canAdd = false, canSubtract = true),
                stepByOne = Step(canAdd = true, canSubtract = false),
                onIncrement = {}
            )
        }

        composeTestRule.onNodeWithText("+10").assertIsNotEnabled()
        composeTestRule.onNodeWithText("-10").assertIsEnabled()
        composeTestRule.onNodeWithText("+1").assertIsEnabled()
        composeTestRule.onNodeWithText("-1").assertIsNotEnabled()
    }

    @Test
    fun `BonusRow displays text and calls onRemove`() {
        var removed = false
        composeTestRule.setContent {
            BonusRow(
                text = "Belote",
                onRemove = { removed = true }
            )
        }

        composeTestRule.onNodeWithText("Belote").assertExists()
        composeTestRule.onNodeWithText("Belote").assertIsEnabled()
        assertEquals(true, !removed)
    }
}

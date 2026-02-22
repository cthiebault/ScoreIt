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

package com.sbgapps.scoreit.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val Green600 = Color(0xFF43A047)
private val Green400 = Color(0xFF66BB6A)
private val Green300 = Color(0xFF81C784)
private val Orange500 = Color(0xFFFF9800)
private val Orange300 = Color(0xFFFFB74D)
private val Grey50 = Color(0xFFFAFAFA)
private val Grey800 = Color(0xFF424242)
private val White = Color(0xFFFFFFFF)
private val Red400 = Color(0xFFEF5350)
private val Red300 = Color(0xFFE57373)

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = White,
    secondary = Orange500,
    onSecondary = White,
    surface = Grey50,
    onSurface = Grey800,
    background = White,
    onBackground = Grey800,
)

private val DarkColorScheme = darkColorScheme(
    primary = Green400,
    onPrimary = White,
    secondary = Orange300,
    onSecondary = White,
    surface = Color(0xFF121212),
    onSurface = White,
    background = Color(0xFF1A1A1A),
    onBackground = White,
)

@Immutable
data class ScoreItColors(
    val gameWon: Color,
    val gameLost: Color,
)

val LocalScoreItColors = staticCompositionLocalOf {
    ScoreItColors(
        gameWon = Color.Unspecified,
        gameLost = Color.Unspecified,
    )
}

private val LightScoreItColors = ScoreItColors(
    gameWon = Green400,
    gameLost = Red400,
)

private val DarkScoreItColors = ScoreItColors(
    gameWon = Green300,
    gameLost = Red300,
)

@Composable
fun ScoreItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme
    val scoreItColors: ScoreItColors

    if (darkTheme) {
        colorScheme = DarkColorScheme
        scoreItColors = DarkScoreItColors
    } else {
        colorScheme = LightColorScheme
        scoreItColors = LightScoreItColors
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalScoreItColors provides scoreItColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

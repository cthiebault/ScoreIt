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

package com.sbgapps.scoreit.app.ui.prefs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sbgapps.scoreit.R
import com.sbgapps.scoreit.app.ui.history.HistoryActivity
import com.sbgapps.scoreit.app.ui.theme.ScoreItTheme
import com.sbgapps.scoreit.core.ext.start
import com.sbgapps.scoreit.core.utils.THEME_MODE_AUTO
import com.sbgapps.scoreit.core.utils.THEME_MODE_DARK
import com.sbgapps.scoreit.core.utils.THEME_MODE_LIGHT
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreferencesActivity : ComponentActivity() {

    private val viewModel by viewModel<PreferencesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(viewModel.getThemeMode())
        setContent {
            ScoreItTheme {
                PreferencesScreen(
                    viewModel = viewModel,
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                    onThemeChanged = { mode ->
                        viewModel.setPrefThemeMode(mode)
                        start<HistoryActivity> {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencesScreen(
    viewModel: PreferencesViewModel,
    onBack: () -> Unit,
    onThemeChanged: (String) -> Unit,
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTotals by remember { mutableStateOf(viewModel.isUniversalTotalDisplayed()) }
    var roundBelote by remember { mutableStateOf(viewModel.isBeloteScoreRounded()) }
    var roundCoinche by remember { mutableStateOf(viewModel.isCoincheScoreRounded()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.prefs_select_theme_mode),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDialog = true }
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            SectionHeader(stringResource(R.string.title_game_universal))
            SwitchPreference(
                text = stringResource(R.string.menu_action_show_totals),
                checked = showTotals,
                onCheckedChange = {
                    showTotals = it
                    viewModel.setUniversalTotalDisplayed(it)
                }
            )

            SectionHeader(stringResource(R.string.title_game_belote))
            SwitchPreference(
                text = stringResource(R.string.menu_action_round_scores),
                checked = roundBelote,
                onCheckedChange = {
                    roundBelote = it
                    viewModel.setBeloteScoreRounded(it)
                }
            )

            SectionHeader(stringResource(R.string.title_game_coinche))
            SwitchPreference(
                text = stringResource(R.string.menu_action_round_scores),
                checked = roundCoinche,
                onCheckedChange = {
                    roundCoinche = it
                    viewModel.setCoincheScoreRounded(it)
                }
            )
        }
    }

    if (showThemeDialog) {
        val currentChoice = when (viewModel.getPrefThemeMode()) {
            THEME_MODE_LIGHT -> 0
            THEME_MODE_DARK -> 1
            else -> 2
        }
        ThemeModeDialog(
            currentChoice = currentChoice,
            onDismiss = { showThemeDialog = false },
            onSelected = { which ->
                showThemeDialog = false
                val mode = when (which) {
                    0 -> THEME_MODE_LIGHT
                    1 -> THEME_MODE_DARK
                    else -> THEME_MODE_AUTO
                }
                onThemeChanged(mode)
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
    )
}

@Composable
private fun SwitchPreference(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeModeDialog(
    currentChoice: Int,
    onDismiss: () -> Unit,
    onSelected: (Int) -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(currentChoice) }
    val options = listOf(
        stringResource(com.sbgapps.scoreit.core.R.string.settings_light_mode),
        stringResource(com.sbgapps.scoreit.core.R.string.settings_dark_mode),
        stringResource(com.sbgapps.scoreit.core.R.string.settings_battery_mode),
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prefs_select_theme_mode)) },
        text = {
            Column {
                options.forEachIndexed { index, label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(index) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelected(index) }
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

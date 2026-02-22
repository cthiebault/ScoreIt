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

package com.sbgapps.scoreit.app.ui.edition.belote

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sbgapps.scoreit.R
import com.sbgapps.scoreit.app.ui.edition.BonusRow
import com.sbgapps.scoreit.app.ui.edition.EditionActivity
import com.sbgapps.scoreit.app.ui.edition.PointsStepper
import com.sbgapps.scoreit.app.ui.theme.ScoreItTheme
import com.sbgapps.scoreit.core.utils.string.build
import com.sbgapps.scoreit.data.model.BeloteBonusValue
import com.sbgapps.scoreit.data.model.PlayerPosition
import org.koin.androidx.viewmodel.ext.android.viewModel

class BeloteEditionActivity : EditionActivity() {

    private val viewModel by viewModel<BeloteEditionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) { viewModel.cancelEdition() }

        setContent {
            ScoreItTheme {
                val state by viewModel.states.collectAsState(initial = null)

                when (state) {
                    is BeloteEditionState.Completed -> finish()
                    is BeloteEditionState.Content -> BeloteEditionScreen(
                        content = state as BeloteEditionState.Content,
                        onBack = { viewModel.cancelEdition() },
                        onDone = { viewModel.completeEdition() },
                        onTakerChanged = { viewModel.changeTaker(it) },
                        onIncrement = { viewModel.incrementScore(it) },
                        onAddBonus = { viewModel.addBonus(it) },
                        onRemoveBonus = { viewModel.removeBonus(it) }
                    )
                    else -> {}
                }
            }
        }
        viewModel.loadContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeloteEditionScreen(
    content: BeloteEditionState.Content,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onTakerChanged: (PlayerPosition) -> Unit,
    onIncrement: (Int) -> Unit,
    onAddBonus: (Pair<PlayerPosition, BeloteBonusValue>) -> Unit,
    onRemoveBonus: (Int) -> Unit,
) {
    val context = LocalContext.current
    var showBonusSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.Check, contentDescription = null)
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
            // Taker selection
            Text(
                text = stringResource(R.string.belote_header_taker),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = content.taker == PlayerPosition.ONE,
                    onClick = { onTakerChanged(PlayerPosition.ONE) },
                    label = { Text(content.players[PlayerPosition.ONE.index].name) }
                )
                FilterChip(
                    selected = content.taker == PlayerPosition.TWO,
                    onClick = { onTakerChanged(PlayerPosition.TWO) },
                    label = { Text(content.players[PlayerPosition.TWO.index].name) }
                )
            }

            // Info
            Text(
                text = content.lapInfo.build(context),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Points
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.belote_points_taker), style = MaterialTheme.typography.labelMedium)
                    Text(content.results.first, style = MaterialTheme.typography.headlineMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.belote_points_defender), style = MaterialTheme.typography.labelMedium)
                    Text(content.results.second, style = MaterialTheme.typography.headlineMedium)
                }
            }

            PointsStepper(
                label = stringResource(R.string.belote_header_points),
                pointsText = content.results.first,
                stepByTen = content.stepPointsByTen,
                stepByOne = content.stepPointsByOne,
                onIncrement = onIncrement
            )

            // Bonuses
            if (content.availableBonuses.isNotEmpty()) {
                TextButton(
                    onClick = { showBonusSheet = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.belote_header_bonuses))
                }
            }

            content.selectedBonuses.forEachIndexed { index, (player, bonus) ->
                BonusRow(
                    text = "${content.players[player.index].name} • ${context.getString(bonus.resId)}",
                    onRemove = { onRemoveBonus(index) }
                )
            }
        }
    }

    if (showBonusSheet) {
        BeloteBonusSheet(
            content = content,
            onDismiss = { showBonusSheet = false },
            onAddBonus = { bonus ->
                onAddBonus(bonus)
                showBonusSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeloteBonusSheet(
    content: BeloteEditionState.Content,
    onDismiss: () -> Unit,
    onAddBonus: (Pair<PlayerPosition, BeloteBonusValue>) -> Unit,
) {
    val context = LocalContext.current
    var selectedTeam by remember { mutableIntStateOf(0) }
    var selectedBonusIndex by remember { mutableIntStateOf(0) }
    var showBonusDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTeam == 0,
                    onClick = { selectedTeam = 0 },
                    label = { Text(content.players[PlayerPosition.ONE.index].name) }
                )
                FilterChip(
                    selected = selectedTeam == 1,
                    onClick = { selectedTeam = 1 },
                    label = { Text(content.players[PlayerPosition.TWO.index].name) }
                )
            }

            TextButton(onClick = { showBonusDialog = true }) {
                Text(context.getString(content.availableBonuses.getOrElse(selectedBonusIndex) { content.availableBonuses.first() }.resId))
            }

            TextButton(onClick = {
                val team = if (selectedTeam == 0) PlayerPosition.ONE else PlayerPosition.TWO
                val bonus = content.availableBonuses.getOrElse(selectedBonusIndex) { content.availableBonuses.first() }
                onAddBonus(team to bonus)
            }) {
                Text(stringResource(R.string.button_action_add))
            }
        }
    }

    if (showBonusDialog) {
        AlertDialog(
            onDismissRequest = { showBonusDialog = false },
            confirmButton = {},
            text = {
                Column {
                    content.availableBonuses.forEachIndexed { index, bonus ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == selectedBonusIndex,
                                onClick = {
                                    selectedBonusIndex = index
                                    showBonusDialog = false
                                }
                            )
                            Text(context.getString(bonus.resId))
                        }
                    }
                }
            }
        )
    }
}

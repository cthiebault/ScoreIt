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

package com.sbgapps.scoreit.app.ui.edition.tarot

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
import com.sbgapps.scoreit.data.model.PlayerPosition
import com.sbgapps.scoreit.data.model.TarotBidValue
import com.sbgapps.scoreit.data.model.TarotBonusValue
import com.sbgapps.scoreit.data.model.TarotOudlerValue
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat

class TarotEditionActivity : EditionActivity() {

    private val viewModel by viewModel<TarotEditionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) { viewModel.cancelEdition() }

        setContent {
            ScoreItTheme {
                val state by viewModel.states.collectAsState(initial = null)

                when (state) {
                    is TarotEditionState.Completed -> finish()
                    is TarotEditionState.Content -> TarotEditionScreen(
                        content = state as TarotEditionState.Content,
                        onBack = { viewModel.cancelEdition() },
                        onDone = { viewModel.completeEdition() },
                        onSetTaker = { viewModel.setTaker(it) },
                        onSetPartner = { viewModel.setPartner(it) },
                        onSetBid = { viewModel.setBid(it) },
                        onSetOudlers = { viewModel.setOudlers(it) },
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
private fun TarotEditionScreen(
    content: TarotEditionState.Content,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onSetTaker: (PlayerPosition) -> Unit,
    onSetPartner: (PlayerPosition) -> Unit,
    onSetBid: (TarotBidValue) -> Unit,
    onSetOudlers: (List<TarotOudlerValue>) -> Unit,
    onIncrement: (Int) -> Unit,
    onAddBonus: (Pair<PlayerPosition, TarotBonusValue>) -> Unit,
    onRemoveBonus: (Int) -> Unit,
) {
    val context = LocalContext.current
    var showTakerDialog by remember { mutableStateOf(false) }
    var showPartnerDialog by remember { mutableStateOf(false) }
    var showBidDialog by remember { mutableStateOf(false) }
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
            // Taker
            Text(
                text = stringResource(R.string.tarot_header_scorer),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            TextButton(
                onClick = { showTakerDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(content.players[content.taker.index].name)
            }

            // Partner (only for 5-player game)
            if (content.partner != PlayerPosition.NONE) {
                Text(
                    text = stringResource(R.string.tarot_header_partner),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                TextButton(
                    onClick = { showPartnerDialog = true },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(content.players[content.partner.index].name)
                }
            }

            // Bid
            Text(
                text = stringResource(R.string.tarot_header_bid),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            TextButton(
                onClick = { showBidDialog = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(stringResource(content.bid.resId))
            }

            // Oudlers
            Text(
                text = stringResource(R.string.tarot_header_oudlers),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = content.oudlers.contains(TarotOudlerValue.PETIT),
                    onClick = {
                        val oudlers = content.oudlers.toMutableList()
                        if (oudlers.contains(TarotOudlerValue.PETIT)) oudlers.remove(TarotOudlerValue.PETIT)
                        else oudlers.add(TarotOudlerValue.PETIT)
                        onSetOudlers(oudlers)
                    },
                    label = { Text(stringResource(R.string.tarot_oudler_petit)) }
                )
                FilterChip(
                    selected = content.oudlers.contains(TarotOudlerValue.TWENTY_ONE),
                    onClick = {
                        val oudlers = content.oudlers.toMutableList()
                        if (oudlers.contains(TarotOudlerValue.TWENTY_ONE)) oudlers.remove(TarotOudlerValue.TWENTY_ONE)
                        else oudlers.add(TarotOudlerValue.TWENTY_ONE)
                        onSetOudlers(oudlers)
                    },
                    label = { Text(stringResource(R.string.tarot_oudler_twentyone)) }
                )
                FilterChip(
                    selected = content.oudlers.contains(TarotOudlerValue.EXCUSE),
                    onClick = {
                        val oudlers = content.oudlers.toMutableList()
                        if (oudlers.contains(TarotOudlerValue.EXCUSE)) oudlers.remove(TarotOudlerValue.EXCUSE)
                        else oudlers.add(TarotOudlerValue.EXCUSE)
                        onSetOudlers(oudlers)
                    },
                    label = { Text(stringResource(R.string.tarot_oudler_excuse)) }
                )
            }

            // Points
            PointsStepper(
                label = stringResource(R.string.tarot_header_points),
                pointsText = NumberFormat.getInstance().format(content.points),
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

    // Taker selection dialog
    if (showTakerDialog) {
        AlertDialog(
            onDismissRequest = { showTakerDialog = false },
            confirmButton = {},
            text = {
                Column {
                    content.players.forEachIndexed { index, player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = content.taker.index == index,
                                onClick = {
                                    onSetTaker(PlayerPosition.fromIndex(index))
                                    showTakerDialog = false
                                }
                            )
                            Text(player.name)
                        }
                    }
                }
            }
        )
    }

    // Partner selection dialog
    if (showPartnerDialog) {
        AlertDialog(
            onDismissRequest = { showPartnerDialog = false },
            confirmButton = {},
            text = {
                Column {
                    content.players.forEachIndexed { index, player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = content.partner.index == index,
                                onClick = {
                                    onSetPartner(PlayerPosition.fromIndex(index))
                                    showPartnerDialog = false
                                }
                            )
                            Text(player.name)
                        }
                    }
                }
            }
        )
    }

    // Bid selection dialog
    if (showBidDialog) {
        AlertDialog(
            onDismissRequest = { showBidDialog = false },
            confirmButton = {},
            text = {
                Column {
                    TarotBidValue.entries.forEach { bid ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = bid == content.bid,
                                onClick = {
                                    onSetBid(bid)
                                    showBidDialog = false
                                }
                            )
                            Text(stringResource(bid.resId))
                        }
                    }
                }
            }
        )
    }

    if (showBonusSheet) {
        TarotBonusSheet(
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
private fun TarotBonusSheet(
    content: TarotEditionState.Content,
    onDismiss: () -> Unit,
    onAddBonus: (Pair<PlayerPosition, TarotBonusValue>) -> Unit,
) {
    val context = LocalContext.current
    var selectedPlayerIndex by remember { mutableIntStateOf(content.taker.index) }
    var selectedBonusIndex by remember { mutableIntStateOf(0) }
    var showPlayerDialog by remember { mutableStateOf(false) }
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
            TextButton(onClick = { showPlayerDialog = true }) {
                Text(content.players[selectedPlayerIndex].name)
            }

            TextButton(onClick = { showBonusDialog = true }) {
                Text(context.getString(content.availableBonuses.getOrElse(selectedBonusIndex) { content.availableBonuses.first() }.resId))
            }

            TextButton(onClick = {
                val player = PlayerPosition.fromIndex(selectedPlayerIndex)
                val bonus = content.availableBonuses.getOrElse(selectedBonusIndex) { content.availableBonuses.first() }
                onAddBonus(player to bonus)
            }) {
                Text(stringResource(R.string.button_action_add))
            }
        }
    }

    if (showPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            confirmButton = {},
            text = {
                Column {
                    content.players.forEachIndexed { index, player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == selectedPlayerIndex,
                                onClick = {
                                    selectedPlayerIndex = index
                                    showPlayerDialog = false
                                }
                            )
                            Text(player.name)
                        }
                    }
                }
            }
        )
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

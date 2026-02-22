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

package com.sbgapps.scoreit.app.ui.history

import android.app.ActivityOptions
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.sbgapps.scoreit.R
import com.sbgapps.scoreit.app.model.BeloteLapRow
import com.sbgapps.scoreit.app.model.CoincheLapRow
import com.sbgapps.scoreit.app.model.DonationRow
import com.sbgapps.scoreit.app.model.Header
import com.sbgapps.scoreit.app.model.LapRow
import com.sbgapps.scoreit.app.model.TarotLapRow
import com.sbgapps.scoreit.app.model.CactusLapRow
import com.sbgapps.scoreit.app.model.UniversalLapRow
import com.sbgapps.scoreit.app.ui.AboutActivity
import com.sbgapps.scoreit.app.ui.Content
import com.sbgapps.scoreit.app.ui.GameEvent
import com.sbgapps.scoreit.app.ui.GameViewModel
import com.sbgapps.scoreit.app.ui.MenuItem
import com.sbgapps.scoreit.app.ui.chart.ChartViewModel
import com.sbgapps.scoreit.app.ui.edition.belote.BeloteEditionActivity
import com.sbgapps.scoreit.app.ui.edition.coinche.CoincheEditionActivity
import com.sbgapps.scoreit.app.ui.edition.tarot.TarotEditionActivity
import com.sbgapps.scoreit.app.ui.edition.cactus.CactusEditionActivity
import com.sbgapps.scoreit.app.ui.edition.universal.UniversalEditionActivity
import com.sbgapps.scoreit.app.ui.prefs.PreferencesActivity
import com.sbgapps.scoreit.app.ui.prefs.PreferencesViewModel
import com.sbgapps.scoreit.app.ui.saved.SavedGameActivity
import com.sbgapps.scoreit.app.ui.scoreboard.ScoreboardActivity
import com.sbgapps.scoreit.app.ui.theme.LocalScoreItColors
import com.sbgapps.scoreit.app.ui.theme.ScoreItTheme
import com.sbgapps.scoreit.app.ui.widget.LinePoint
import com.sbgapps.scoreit.app.ui.widget.LineSet
import com.sbgapps.scoreit.core.ext.start
import com.sbgapps.scoreit.core.utils.string.build
import com.sbgapps.scoreit.data.model.GameType
import com.sbgapps.scoreit.data.repository.BillingRepo
import com.sbgapps.scoreit.data.repository.Donation
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat

class HistoryActivity : ComponentActivity() {

    private val gameViewModel by viewModel<GameViewModel>()
    private val prefsViewModel by viewModel<PreferencesViewModel>()
    private val chartViewModel by viewModel<ChartViewModel>()
    private val billingRepository by inject<BillingRepo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false

        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(prefsViewModel.getThemeMode())

        setContent {
            ScoreItTheme {
                HistoryScreen(
                    gameViewModel = gameViewModel,
                    chartViewModel = chartViewModel,
                    billingRepository = billingRepository,
                    onStartEdition = { startEdition(it) },
                    onNavigate = { navigateTo(it) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.loadGame()
    }

    private fun startEdition(gameType: GameType) {
        val bundle = ActivityOptions.makeSceneTransitionAnimation(
            this,
            findViewById(android.R.id.content),
            "shared_element_container"
        ).toBundle()
        when (gameType) {
            GameType.UNIVERSAL -> start<UniversalEditionActivity>(bundle)
            GameType.TAROT -> start<TarotEditionActivity>(bundle)
            GameType.BELOTE -> start<BeloteEditionActivity>(bundle)
            GameType.COINCHE -> start<CoincheEditionActivity>(bundle)
            GameType.CACTUS -> start<CactusEditionActivity>(bundle)
        }
    }

    private fun navigateTo(destination: NavigationDestination) {
        when (destination) {
            NavigationDestination.Scoreboard -> start<ScoreboardActivity>()
            NavigationDestination.Preferences -> start<PreferencesActivity>()
            NavigationDestination.About -> start<AboutActivity>()
            NavigationDestination.SavedGames -> start<SavedGameActivity>()
        }
    }
}

enum class NavigationDestination { Scoreboard, Preferences, About, SavedGames }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(
    gameViewModel: GameViewModel,
    chartViewModel: ChartViewModel,
    billingRepository: BillingRepo,
    onStartEdition: (GameType) -> Unit,
    onNavigate: (NavigationDestination) -> Unit,
) {
    val state by gameViewModel.states.collectAsState(initial = null)
    val content = state as? Content

    var showNavDrawer by remember { mutableStateOf(false) }
    var showChart by remember { mutableStateOf(false) }
    var showPlayerCountDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var editingPlayerPosition by remember { mutableIntStateOf(-1) }
    var showPlayerOptionsDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var nameDialogForNewGame by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Handle effects
    LaunchedEffect(Unit) {
        gameViewModel.effects.collect { event ->
            when (event) {
                is GameEvent.Edition -> onStartEdition(event.gameType)
                is GameEvent.Deletion -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.snackbar_msg_on_lap_deleted),
                        actionLabel = context.getString(R.string.snackbar_action_on_lap_deleted),
                        duration = SnackbarDuration.Long
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> gameViewModel.undoDeletion()
                        SnackbarResult.Dismissed -> gameViewModel.confirmDeletion()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { showNavDrawer = true }) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }

                    if (content != null) {
                        val enabledItems = gameViewModel.getEnabledMenuItems()

                        if (MenuItem.PLAYER_COUNT in enabledItems) {
                            IconButton(onClick = { showPlayerCountDialog = true }) {
                                Icon(painterResource(R.drawable.ic_people_24dp), contentDescription = null)
                            }
                        }
                        if (MenuItem.CHART in enabledItems) {
                            IconButton(onClick = { showChart = true }) {
                                Icon(painterResource(R.drawable.ic_show_chart_24dp), contentDescription = null)
                            }
                        }
                        if (MenuItem.CLEAR in enabledItems) {
                            IconButton(onClick = { showClearDialog = true }) {
                                Icon(painterResource(R.drawable.ic_clear_all_24dp), contentDescription = null)
                            }
                        }
                        if (MenuItem.SAVED_GAMES in enabledItems) {
                            IconButton(onClick = { onNavigate(NavigationDestination.SavedGames) }) {
                                Icon(painterResource(R.drawable.ic_unarchive_24dp), contentDescription = null)
                            }
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { gameViewModel.addLap() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (content != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header
                item {
                    HeaderRow(
                        header = content.header,
                        onPlayerClick = { position ->
                            if (gameViewModel.canEditPlayer(position)) {
                                editingPlayerPosition = position
                                showPlayerOptionsDialog = true
                            }
                        }
                    )
                    HorizontalDivider()
                }

                // Laps
                itemsIndexed(
                    items = content.results,
                    key = { index, lap -> "${lap::class.simpleName}_$index" }
                ) { index, lap ->
                    if (lap is DonationRow) {
                        DonationLapRow(
                            onDonate = { donation ->
                                billingRepository.startBillingFlow(
                                    context as HistoryActivity,
                                    donation
                                ) { gameViewModel.onDonationPerformed() }
                            }
                        )
                    } else {
                        SwipeableLapRow(
                            lap = lap,
                            onEdit = {
                                gameViewModel.editLap(index)
                            },
                            onDelete = {
                                gameViewModel.deleteLap(index)
                            },
                            onTap = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.history_lap_click_hint),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }

                    if (index < content.results.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // Nav drawer
    if (showNavDrawer) {
        NavDrawerSheet(
            onDismiss = { showNavDrawer = false },
            onSelectGame = { gameType ->
                gameViewModel.selectGame(gameType)
                showNavDrawer = false
            },
            onNavigate = { dest ->
                showNavDrawer = false
                onNavigate(dest)
            }
        )
    }

    // Chart
    if (showChart) {
        ChartSheet(
            chartViewModel = chartViewModel,
            onDismiss = { showChart = false }
        )
    }

    // Player count dialog
    if (showPlayerCountDialog) {
        val options = gameViewModel.getPlayerCountOptions()
        AlertDialog(
            onDismissRequest = { showPlayerCountDialog = false },
            title = { Text(stringResource(R.string.dialog_title_player_number)) },
            confirmButton = {},
            text = {
                Column {
                    options.forEach { count ->
                        TextButton(
                            onClick = {
                                gameViewModel.setPlayerCount(count)
                                showPlayerCountDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(count.toString())
                        }
                    }
                }
            }
        )
    }

    // Clear dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            confirmButton = {},
            text = {
                Column {
                    TextButton(
                        onClick = {
                            gameViewModel.resetGame()
                            showClearDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.dialog_action_game_reset))
                    }
                    TextButton(
                        onClick = {
                            showClearDialog = false
                            nameDialogForNewGame = true
                            showNameDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.dialog_action_game_create))
                    }
                }
            }
        )
    }

    // Name dialog (for new game creation or player name edit)
    if (showNameDialog) {
        NameEditDialog(
            onDismiss = { showNameDialog = false },
            onConfirm = { name ->
                if (name.isNotEmpty()) {
                    if (nameDialogForNewGame) {
                        gameViewModel.createGame(name)
                        nameDialogForNewGame = false
                    } else {
                        gameViewModel.setPlayerName(editingPlayerPosition, name)
                    }
                }
                showNameDialog = false
            }
        )
    }

    // Player edit options dialog
    if (showPlayerOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerOptionsDialog = false },
            confirmButton = {},
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showPlayerOptionsDialog = false
                            nameDialogForNewGame = false
                            showNameDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.dialog_action_enter_name))
                    }
                    TextButton(
                        onClick = {
                            showPlayerOptionsDialog = false
                            showColorPicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.dialog_action_pick_color))
                    }
                }
            }
        )
    }

    // Color picker
    if (showColorPicker) {
        ColorPickerSheet(
            onDismiss = { showColorPicker = false },
            onColorSelected = { color ->
                gameViewModel.setPlayerColor(editingPlayerPosition, color)
                showColorPicker = false
            }
        )
    }
}

@Composable
private fun HeaderRow(
    header: Header,
    onPlayerClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        header.players.forEachIndexed { index, player ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPlayerClick(index) }
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(player.color),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = NumberFormat.getInstance().format(header.scores[index]),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                if (header.markers[index]) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                } else {
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableLapRow(
    lap: LapRow,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTap: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onDelete()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.EndToStart -> {
                onEdit()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.Settled -> {}
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.secondary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                },
                label = "swipe_bg"
            )
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Delete
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Edit
                else -> Icons.Default.Edit
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
        }
    ) {
        LapRowContent(lap = lap, onClick = onTap)
    }
}

@Composable
private fun LapRowContent(lap: LapRow, onClick: () -> Unit) {
    val scoreItColors = LocalScoreItColors.current
    val context = LocalContext.current

    when (lap) {
        is UniversalLapRow -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                lap.results.forEach { result ->
                    Text(
                        text = result.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        is BeloteLapRow -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(if (lap.isWon) scoreItColors.gameWon else scoreItColors.gameLost)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    lap.results.forEach { result ->
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        is CoincheLapRow -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(if (lap.isWon) scoreItColors.gameWon else scoreItColors.gameLost)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    lap.results.forEach { result ->
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        is CactusLapRow -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                lap.results.forEach { result ->
                    Text(
                        text = result.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        is TarotLapRow -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onClick)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(48.dp)
                            .background(if (lap.isWon) scoreItColors.gameWon else scoreItColors.gameLost)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = lap.info.build(context),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            lap.results.forEach { result ->
                                Text(
                                    text = result,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        is DonationRow -> {}
    }
}

@Composable
private fun DonationLapRow(onDonate: (Donation) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(onClick = { onDonate(Donation.COFFEE) }) {
            Text(Donation.COFFEE.name)
        }
        TextButton(onClick = { onDonate(Donation.BEER) }) {
            Text(Donation.BEER.name)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavDrawerSheet(
    onDismiss: () -> Unit,
    onSelectGame: (GameType) -> Unit,
    onNavigate: (NavigationDestination) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.drawer_game_universal),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGame(GameType.UNIVERSAL) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            Text(
                text = stringResource(R.string.drawer_game_cactus),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGame(GameType.CACTUS) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            Text(
                text = stringResource(R.string.drawer_game_tarot),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGame(GameType.TAROT) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            Text(
                text = stringResource(R.string.drawer_game_belote),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGame(GameType.BELOTE) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            Text(
                text = stringResource(R.string.drawer_game_coinche),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectGame(GameType.COINCHE) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.drawer_game_scoreboard),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(NavigationDestination.Scoreboard) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = stringResource(R.string.drawer_preferences),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(NavigationDestination.Preferences) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
            Text(
                text = stringResource(R.string.drawer_about),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(NavigationDestination.About) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartSheet(
    chartViewModel: ChartViewModel,
    onDismiss: () -> Unit,
) {
    val lines by chartViewModel.lines.collectAsState()

    LaunchedEffect(Unit) { chartViewModel.loadLines() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        if (lines.isNotEmpty()) {
            ComposeLineChart(
                lines = lines,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun ComposeLineChart(
    lines: List<LineSet>,
    modifier: Modifier = Modifier,
) {
    val maxY = lines.maxOfOrNull { it.getMaxY() } ?: 0f
    val minY = lines.minOfOrNull { it.getMinY() } ?: 0f
    val maxX = lines.maxOfOrNull { it.getMaxX() } ?: 0f
    val minX = lines.minOfOrNull { it.getMinX() } ?: 0f

    val axisColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = modifier.drawBehind {
            val padding = 32.dp.toPx()
            val usableHeight = size.height - 2 * padding
            val usableWidth = size.width - 2 * padding

            // X-axis
            val yRange = if (maxY == minY) 1f else maxY - minY
            val xAxisY = if (minY < 0) {
                size.height - padding - usableHeight * (-minY / yRange)
            } else {
                size.height - padding
            }

            drawLine(
                color = axisColor,
                start = Offset(padding, xAxisY),
                end = Offset(size.width - padding, xAxisY),
                strokeWidth = 1.dp.toPx()
            )

            val xRange = if (maxX == minX) 1f else maxX - minX

            // Lines and points
            lines.forEach { line ->
                val lineColor = Color(line.color)
                val points = line.points

                for (i in 1 until points.size) {
                    val prevX = padding + ((points[i - 1].x - minX) / xRange) * usableWidth
                    val prevY = size.height - padding - ((points[i - 1].y - minY) / yRange) * usableHeight
                    val currX = padding + ((points[i].x - minX) / xRange) * usableWidth
                    val currY = size.height - padding - ((points[i].y - minY) / yRange) * usableHeight
                    drawLine(lineColor, Offset(prevX, prevY), Offset(currX, currY), strokeWidth = 2.dp.toPx())
                }

                if (line.arePointsDisplayed) {
                    points.forEach { point ->
                        val px = padding + ((point.x - minX) / xRange) * usableWidth
                        val py = size.height - padding - ((point.y - minY) / yRange) * usableHeight
                        drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(px, py))
                    }
                }
            }
        }
    )
}

@Composable
private fun NameEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(stringResource(R.string.button_action_ok))
            }
        },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerSheet(
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit,
) {
    val context = LocalContext.current
    val colors = remember {
        context.resources.getIntArray(R.array.colors).toList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(color), CircleShape)
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

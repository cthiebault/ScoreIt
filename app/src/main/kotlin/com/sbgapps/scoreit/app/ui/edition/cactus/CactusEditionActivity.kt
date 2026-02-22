package com.sbgapps.scoreit.app.ui.edition.cactus

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sbgapps.scoreit.app.ui.edition.EditionActivity
import com.sbgapps.scoreit.app.ui.theme.ScoreItTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class CactusEditionActivity : EditionActivity() {

    private val viewModel by viewModel<CactusEditionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this) { viewModel.cancelEdition() }

        setContent {
            ScoreItTheme {
                val state by viewModel.states.collectAsState(initial = null)

                when (state) {
                    is CactusEditionState.Completed -> finish()
                    is CactusEditionState.Content -> CactusEditionScreen(
                        content = state as CactusEditionState.Content,
                        onBack = { viewModel.cancelEdition() },
                        onDone = { viewModel.completeEdition() },
                        onIncrement = { position, increment -> viewModel.incrementScore(increment, position) },
                        onScoreSet = { position, score -> viewModel.setScore(position, score) }
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
private fun CactusEditionScreen(
    content: CactusEditionState.Content,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onIncrement: (Int, Int) -> Unit,
    onScoreSet: (Int, Int) -> Unit,
) {
    var inputScorePosition by remember { mutableIntStateOf(-1) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            itemsIndexed(content.players) { index, player ->
                PlayerScoreRow(
                    name = player.name,
                    score = content.results[index],
                    onIncrement = { increment -> onIncrement(index, increment) },
                    onScoreTap = { inputScorePosition = index }
                )
                if (index < content.players.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }

    if (inputScorePosition >= 0) {
        ScoreInputSheet(
            onDismiss = { inputScorePosition = -1 },
            onConfirm = { score ->
                onScoreSet(inputScorePosition, score)
                inputScorePosition = -1
            }
        )
    }
}

@Composable
private fun PlayerScoreRow(
    name: String,
    score: Int,
    onIncrement: (Int) -> Unit,
    onScoreTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { onIncrement(-1) }, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("-1")
        }
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .weight(0.5f)
        )
        Button(onClick = { onIncrement(1) }, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text("+1")
        }
        Button(onClick = onScoreTap, modifier = Modifier.padding(start = 4.dp)) {
            Text("...")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoreInputSheet(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var text by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val score = text.toIntOrNull() ?: 0
                        onConfirm(score)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val score = text.toIntOrNull() ?: 0
                    onConfirm(score)
                },
                enabled = text.isNotEmpty() && text != "-"
            ) {
                Text("OK")
            }
        }
    }
}

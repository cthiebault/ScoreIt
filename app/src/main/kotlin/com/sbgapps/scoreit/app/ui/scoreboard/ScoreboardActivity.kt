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

package com.sbgapps.scoreit.app.ui.scoreboard

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sbgapps.scoreit.R
import com.sbgapps.scoreit.app.ui.prefs.PreferencesViewModel
import com.sbgapps.scoreit.app.ui.theme.ScoreItTheme
import com.sbgapps.scoreit.data.model.PlayerPosition
import java.text.NumberFormat
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScoreboardActivity : ComponentActivity() {

    private val scoreBoardViewModel by viewModel<ScoreBoardViewModel>()
    private val prefsViewModel by viewModel<PreferencesViewModel>()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(prefsViewModel.getThemeMode())
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContent {
            ScoreItTheme {
                ScoreboardScreen(
                    viewModel = scoreBoardViewModel,
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}

@Composable
private fun ScoreboardScreen(
    viewModel: ScoreBoardViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.states.collectAsState(initial = null)
    val content = state as? Content ?: return
    val nf = remember { NumberFormat.getInstance() }
    var editingPlayer by remember { mutableStateOf<PlayerPosition?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back_24dp),
                contentDescription = null
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = nf.format(content.scoreBoard.scoreOne),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = TextAlign.Center,
                    fontSize = 80.sp
                )
                Text(
                    text = content.scoreBoard.nameOne,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { editingPlayer = PlayerPosition.ONE }
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Button(onClick = { viewModel.incrementScore(-1, PlayerPosition.ONE) }) {
                        Text("-1")
                    }
                    Button(onClick = { viewModel.incrementScore(1, PlayerPosition.ONE) }) {
                        Text("+1")
                    }
                }
            }

            // Separator
            Text(
                text = ":",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.width(96.dp),
                textAlign = TextAlign.Center,
                fontSize = 80.sp
            )

            // Player 2
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = nf.format(content.scoreBoard.scoreTwo),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = TextAlign.Center,
                    fontSize = 80.sp
                )
                Text(
                    text = content.scoreBoard.nameTwo,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { editingPlayer = PlayerPosition.TWO }
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Button(onClick = { viewModel.incrementScore(-1, PlayerPosition.TWO) }) {
                        Text("-1")
                    }
                    Button(onClick = { viewModel.incrementScore(1, PlayerPosition.TWO) }) {
                        Text("+1")
                    }
                }
            }
        }

        IconButton(
            onClick = { viewModel.reset() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh_24dp),
                contentDescription = null
            )
        }
    }

    editingPlayer?.let { position ->
        NameEditDialog(
            onDismiss = { editingPlayer = null },
            onConfirm = { name ->
                if (name.isNotEmpty()) viewModel.setPlayerName(name, position)
                editingPlayer = null
            }
        )
    }
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

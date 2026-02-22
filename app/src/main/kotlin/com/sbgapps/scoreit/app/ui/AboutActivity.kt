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

package com.sbgapps.scoreit.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory
import com.sbgapps.scoreit.BuildConfig
import com.sbgapps.scoreit.R
import com.sbgapps.scoreit.app.ui.theme.ScoreItTheme

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScoreItTheme {
                AboutScreen()
            }
        }
    }
}

@Composable
private fun AboutScreen() {
    val context = LocalContext.current
    var animTrigger by remember { mutableIntStateOf(0) }

    val animProgress by animateFloatAsState(
        targetValue = if (animTrigger > 0) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "about_anim"
    )

    // Trigger the initial animation
    if (animTrigger == 0) animTrigger = 1

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_score_it),
            contentDescription = null,
            modifier = Modifier
                .scale(0.5f + 0.5f * animProgress)
                .alpha(0.2f + 0.8f * animProgress)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { animTrigger++ }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.alpha(animProgress)
        )

        Text(
            text = BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.alpha(animProgress)
        )

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.alpha(animProgress)
        ) {
            IconButton(onClick = {
                val manager = ReviewManagerFactory.create(context)
                manager.requestReviewFlow().addOnCompleteListener {
                    if (it.isSuccessful) {
                        manager.launchReviewFlow(context as AboutActivity, it.result)
                    }
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_rate_review_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:".toUri()
                    putExtra(Intent.EXTRA_EMAIL, "stephane" + "@" + "baiget" + ".fr")
                    putExtra(Intent.EXTRA_SUBJECT, "ScoreIt")
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_email_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://github.com/StephaneBg/ScoreIt".toUri()
                )
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_github_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Stéphane Baiget",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.alpha(animProgress)
        )
    }
}

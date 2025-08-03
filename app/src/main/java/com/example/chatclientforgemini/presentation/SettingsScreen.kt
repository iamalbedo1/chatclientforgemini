package com.example.chatclientforgemini.presentation

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.input.RemoteInputIntentHelper
import kotlinx.coroutines.launch

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    Button(
        onClick = {
            coroutineScope.launch {
                scale.animateTo(0.95f, animationSpec = tween(100))
                scale.animateTo(1f, animationSpec = tween(100))
                onClick()
            }
        },
        modifier = modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        shape = RoundedCornerShape(24.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsScreen(
    onSaveApiKey: (String) -> Unit,
    onSaveSystemPrompt: (String) -> Unit,
    onSaveModel: (String) -> Unit,
    onResetSystemPrompt: () -> Unit,
    currentSystemPrompt: String,
    currentModel: String
) {
    val (showSystemPrompt, setShowSystemPrompt) = remember { mutableStateOf(false) }
    val (showModelSelection, setShowModelSelection) = remember { mutableStateOf(false) }

    val apiKeyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val apiKey = RemoteInput.getResultsFromIntent(result.data)
                ?.getCharSequence("api_key")
                ?.toString()
            if (!apiKey.isNullOrBlank()) {
                onSaveApiKey(apiKey)
            }
        }
    }

    val systemPromptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val systemPrompt = RemoteInput.getResultsFromIntent(result.data)
                ?.getCharSequence("system_prompt")
                ?.toString()
            if (!systemPrompt.isNullOrBlank()) {
                onSaveSystemPrompt(systemPrompt)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val listState = rememberScalingLazyListState()
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 60.dp, bottom = 48.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.title2
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                AnimatedButton(
                    onClick = {
                        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                        val remoteInput = RemoteInput.Builder("api_key")
                            .setLabel("Enter Gemini API Key")
                            .build()
                        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
                        apiKeyLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Set API Key")
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                AnimatedButton(
                    onClick = { setShowModelSelection(true) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Choose model")
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                AnimatedButton(
                    onClick = {
                        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                        val remoteInput = RemoteInput.Builder("system_prompt")
                            .setLabel("Enter System Prompt")
                            .build()
                        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
                        systemPromptLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Set System Prompt")
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                AnimatedButton(
                    onClick = onResetSystemPrompt,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Reset System Prompt")
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                AnimatedButton(
                    onClick = { setShowSystemPrompt(!showSystemPrompt) },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(if (showSystemPrompt) "Hide System Prompt" else "Show System Prompt")
                }
            }
            if (showSystemPrompt) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Text(
                        text = "Current prompt: $currentSystemPrompt",
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Text(
                    text = "Current model: $currentModel",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        if (showModelSelection) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 50) { // Swipe right to dismiss
                                setShowModelSelection(false)
                            }
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedButton(
                        onClick = {
                            onSaveModel("gemini-2.5-flash-lite")
                            setShowModelSelection(false)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
                    ) {
                        Text("gemini-2.5-flash-lite")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedButton(
                        onClick = {
                            onSaveModel("gemini-2.5-flash")
                            setShowModelSelection(false)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
                    ) {
                        Text("gemini-2.5-flash")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedButton(
                        onClick = {
                            onSaveModel("gemini-2.5-pro")
                            setShowModelSelection(false)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp)
                    ) {
                        Text("gemini-2.5-pro")
                    }
                }
            }
        }
    }
}
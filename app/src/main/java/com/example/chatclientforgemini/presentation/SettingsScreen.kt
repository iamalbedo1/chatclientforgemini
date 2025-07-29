package com.example.chatclientforgemini.presentation

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.input.RemoteInputIntentHelper

@Composable
fun SettingsScreen(
    onSave: (String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val apiKey = RemoteInput.getResultsFromIntent(result.data)
                ?.getCharSequence("api_key")
                ?.toString()
            if (!apiKey.isNullOrBlank()) {
                onSave(apiKey)
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
                    text = "Gemini API Key",
                    style = MaterialTheme.typography.title2
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Button(
                    onClick = {
                        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                        val remoteInput = RemoteInput.Builder("api_key")
                            .setLabel("Enter Gemini API Key")
                            .build()
                        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
                        launcher.launch(intent)
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Set API Key")
                }
            }
        }
    }
}
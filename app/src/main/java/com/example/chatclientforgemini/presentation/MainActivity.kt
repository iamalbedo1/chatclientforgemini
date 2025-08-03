package com.example.chatclientforgemini.presentation

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.PagerState
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material.Icon
import com.example.chatclientforgemini.presentation.theme.ChatClientForGeminiTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: ChatViewModel by viewModels()

        setContent {
            ChatClientForGeminiTheme {
                val coroutineScope = rememberCoroutineScope()
                val pagerState = rememberPagerState(initialPage = 1) { 3 }

                val uiState by viewModel.uiState.collectAsState()
                val historyState by viewModel.historyState.collectAsState()
                val hapticEvent by viewModel.hapticEvent.collectAsState(initial = null)
                val currentSystemPrompt by viewModel.currentSystemPrompt.collectAsState()
                val vibrator = getSystemService(Vibrator::class.java)

                LaunchedEffect(hapticEvent) {
                    hapticEvent?.let {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> ChatHistoryScreen(
                                conversations = historyState,
                                onConversationClick = { conversationId ->
                                    viewModel.loadConversation(conversationId)
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                }
                            )
                            1 -> ChatScreen(
                                uiState = uiState,
                                onSend = { viewModel.sendMessage(it) }
                            )
                            2 -> SettingsScreen(
                                onSaveApiKey = { apiKey ->
                                    viewModel.saveApiKey(apiKey)
                                },
                                onSaveSystemPrompt = { systemPrompt ->
                                    viewModel.saveSystemPrompt(systemPrompt)
                                },
                                onResetSystemPrompt = {
                                    viewModel.resetSystemPrompt()
                                },
                                currentSystemPrompt = currentSystemPrompt
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PagerIndicator(pagerState = pagerState)
                        if (pagerState.currentPage == 1 && uiState.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear chat",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { viewModel.clearChat() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class)
@Composable
fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val color by animateColorAsState(
                targetValue = if (index == pagerState.currentPage) Color.White else Color.Gray,
                label = "Pager indicator color"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
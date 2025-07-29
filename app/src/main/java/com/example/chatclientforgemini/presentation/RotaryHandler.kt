package com.example.chatclientforgemini.presentation

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.rotary.RotaryScrollEvent
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RotaryHandler(
    private val coroutineScope: CoroutineScope,
    private val listState: ScalingLazyListState,
    private val focusRequester: FocusRequester
) {
    fun handleScrollEvent(event: RotaryScrollEvent): Boolean {
        val delta = event.verticalScrollPixels
        coroutineScope.launch {
            listState.scrollBy(delta)
        }
        focusRequester.requestFocus()
        return true
    }
}
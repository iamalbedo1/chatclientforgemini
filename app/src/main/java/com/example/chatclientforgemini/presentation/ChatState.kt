package com.example.chatclientforgemini.presentation

import java.util.UUID

data class ChatMessage(
    val text: String,
    val author: Author,
    val isLoading: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Author {
    USER, MODEL
}

data class ChatConversation(
    val id: String = UUID.randomUUID().toString(),
    var title: String?,
    val messages: List<ChatMessage> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor(
        id: String = UUID.randomUUID().toString(),
        messages: List<ChatMessage> = emptyList(),
        timestamp: Long = System.currentTimeMillis()
    ) : this(
        id = id,
        title = messages.firstOrNull { it.author == Author.USER }?.text,
        messages = messages,
        timestamp = timestamp
    )

    val lastUpdated: Long
        get() = messages.lastOrNull()?.timestamp ?: timestamp
}
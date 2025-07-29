package com.example.chatclientforgemini.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val historyRepository = ChatHistoryRepository(application)

    private val defaultSystemPromptText = "You are a helpful AI assistant on a smartwatch. Your primary goal is to provide answers that are direct, factual, and concise, optimized for a small screen. Prioritize being helpful. Use bullet points for lists or step-by-step instructions. Avoid unnecessary disclaimers or apologies."

    private lateinit var generativeModel: GenerativeModel
    private lateinit var chat: Chat
    private var currentConversationId: String? = null

    /* ---------- UI state ---------- */
    private val _uiState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val uiState = _uiState.asStateFlow()

    private val _historyState = MutableStateFlow<List<ChatConversation>>(emptyList())
    val historyState = _historyState.asStateFlow()

    private val _hapticEvent = MutableSharedFlow<Unit>()
    val hapticEvent = _hapticEvent.asSharedFlow()

    init {
        initializeChat()
        loadHistory()
    }

    private fun loadHistory() {
        _historyState.update { historyRepository.loadConversations() }
    }

    private fun initializeChat() {
        _uiState.update { emptyList() }
        currentConversationId = null
        val apiKey = settingsRepository.apiKey
        if (apiKey.isNullOrBlank()) {
            _uiState.update {
                listOf(ChatMessage("No API key provided. Please add one in the settings.", Author.MODEL))
            }
        } else {
            generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
            val systemPrompt = content("user") {
                text(settingsRepository.systemPrompt ?: defaultSystemPromptText)
            }
            chat = generativeModel.startChat(history = listOf(systemPrompt))
        }
    }

    /* ---------- public API ---------- */
    fun saveApiKey(apiKey: String) {
        settingsRepository.apiKey = apiKey
        initializeChat()
    }

    val currentSystemPrompt: String
        get() = settingsRepository.systemPrompt ?: defaultSystemPromptText

    fun saveSystemPrompt(prompt: String) {
        settingsRepository.systemPrompt = prompt
        clearChat()
    }

    fun resetSystemPrompt() {
        settingsRepository.clearSystemPrompt()
        clearChat()
    }

    fun sendMessage(userInput: String) {
        if (!::generativeModel.isInitialized) {
            _uiState.update {
                listOf(ChatMessage("No API key provided. Please add one in the settings.", Author.MODEL))
            }
            return
        }

        /* Add USER + loading stub in one atomic change */
        _uiState.update { list ->
            list + listOf(
                ChatMessage(userInput, Author.USER),
                ChatMessage("…", Author.MODEL, isLoading = true)
            )
        }

        viewModelScope.launch {
            try {
                val replyText = chat.sendMessage(userInput).text ?: "Error"
                handleApiResponse(replyText)
                _hapticEvent.emit(Unit)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage failed", e)
                _uiState.update { list ->
                    list.filterNot { it.isLoading } +
                            ChatMessage("Connection error.", Author.MODEL)
                }
            }
        }
    }

    fun clearChat() {
        saveCurrentChat()
        if (::generativeModel.isInitialized) {
            val systemPrompt = content("user") {
                text(settingsRepository.systemPrompt ?: defaultSystemPromptText)
            }
            chat = generativeModel.startChat(history = listOf(systemPrompt))
            _uiState.update { emptyList() }
            currentConversationId = null
        }
    }

    fun loadConversation(conversationId: String) {
        val conversation = historyRepository.loadConversations().find { it.id == conversationId }
        if (conversation != null) {
            _uiState.update { conversation.messages }
            currentConversationId = conversation.id
            // Re-initialize chat with the history of the loaded conversation
            if (::generativeModel.isInitialized) {
                val chatHistory = conversation.messages.map {
                    content(if (it.author == Author.USER) "user" else "model") { text(it.text) }
                }
                chat = generativeModel.startChat(history = chatHistory)
            }
        }
    }

    private fun saveCurrentChat() {
        val currentMessages = _uiState.value
        if (currentMessages.any { it.author == Author.USER }) {
            val conversation = if (currentConversationId != null) {
                val existing = historyRepository.loadConversations().find { it.id == currentConversationId }
                existing?.copy(messages = currentMessages, timestamp = System.currentTimeMillis())
            } else {
                ChatConversation(messages = currentMessages)
            }

            if (conversation != null) {
                val allConversations = historyRepository.loadConversations().toMutableList()
                val existingIndex = allConversations.indexOfFirst { it.id == conversation.id }
                if (existingIndex != -1) {
                    allConversations[existingIndex] = conversation
                } else {
                    allConversations.add(0, conversation)
                    // Generate title only for new conversations
                    viewModelScope.launch {
                        generateTitle(conversation)
                    }
                }
                historyRepository.saveConversations(allConversations)
                loadHistory()
            }
        }
    }

    private suspend fun generateTitle(conversation: ChatConversation) {
        try {
            val firstUserMessage = conversation.messages.firstOrNull { it.author == Author.USER }?.text ?: return
            val titlePrompt = "Summarize the following text in 3 words or less, to be used as a title: \"$firstUserMessage\""
            val titleResponse = generativeModel.generateContent(titlePrompt).text?.trim()
            if (!titleResponse.isNullOrBlank()) {
                conversation.title = titleResponse
                // Re-save the conversation with the new title
                val allConversations = historyRepository.loadConversations().toMutableList()
                val existingIndex = allConversations.indexOfFirst { it.id == conversation.id }
                if (existingIndex != -1) {
                    allConversations[existingIndex] = conversation
                    historyRepository.saveConversations(allConversations)
                    loadHistory()
                }
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Title generation failed", e)
        }
    }

    private fun handleApiResponse(responseText: String) {
        _uiState.update { list ->
            list.filterNot { it.isLoading } +
                    ChatMessage(responseText, Author.MODEL)
        }
    }
}
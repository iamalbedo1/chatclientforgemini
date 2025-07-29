package com.example.chatclientforgemini.presentation

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatHistoryRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "chat_history_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val gson = Gson()

    fun saveConversations(conversations: List<ChatConversation>) {
        val json = gson.toJson(conversations)
        sharedPreferences.edit {
            putString("history", json)
        }
    }

    fun loadConversations(): List<ChatConversation> {
        val json = sharedPreferences.getString("history", null)
        return if (json != null) {
            val type = object : TypeToken<List<ChatConversation>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
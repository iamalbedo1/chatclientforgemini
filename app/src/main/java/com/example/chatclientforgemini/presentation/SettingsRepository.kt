package com.example.chatclientforgemini.presentation

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences =
        EncryptedSharedPreferences.create(
            context,
            "api_key_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    var apiKey: String?
        get() = prefs.getString("api_key", null)
        set(value) = prefs.edit { putString("api_key", value) }

    var systemPrompt: String?
        get() = prefs.getString("system_prompt", null)
        set(value) = prefs.edit { putString("system_prompt", value) }

    fun clearSystemPrompt() {
        prefs.edit { remove("system_prompt") }
    }
}
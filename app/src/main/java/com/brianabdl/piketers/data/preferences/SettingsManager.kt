package com.brianabdl.piketers.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        val BOT_TOKEN_KEY = stringPreferencesKey("bot_token")
        val TARGET_CHAT_ID_KEY = stringPreferencesKey("target_chat_id")
    }

    // Get the saved bot token
    val botToken: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[BOT_TOKEN_KEY] ?: ""
        }

    // Get the saved target chat ID
    val targetChatId: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TARGET_CHAT_ID_KEY] ?: ""
        }

    // Save bot token
    suspend fun saveBotToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[BOT_TOKEN_KEY] = token
        }
    }

    // Save target chat ID
    suspend fun saveTargetChatId(chatId: String) {
        context.dataStore.edit { preferences ->
            preferences[TARGET_CHAT_ID_KEY] = chatId
        }
    }
}
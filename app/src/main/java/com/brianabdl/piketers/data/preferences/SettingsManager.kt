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

        val MEMBERS_KEY = stringPreferencesKey("members")
        
        // Default members
        val DEFAULT_MEMBERS = listOf(
            "Mas Brian",
            "Mas Ruziq",
            "Mas Falyd",
            "Mas Tanzihan"
        )
    }

    // Get the saved members list
    val members: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val membersString = preferences[MEMBERS_KEY]
            if (membersString.isNullOrBlank()) {
                DEFAULT_MEMBERS
            } else {
                membersString.split("|").filter { it.isNotBlank() }
            }
        }

    // Save members list
    suspend fun saveMembers(membersList: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[MEMBERS_KEY] = membersList.joinToString("|")
        }
    }
}
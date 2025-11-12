package com.brianabdl.piketers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.brianabdl.piketers.data.preferences.SettingsManager
import com.brianabdl.piketers.data.repository.PiketRepository
import com.brianabdl.piketers.ui.navigation.AppNavigation
import com.brianabdl.piketers.ui.theme.PiketersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsManager = remember { SettingsManager(this) }
            val piketRepository = remember { PiketRepository() }

            // Load members from settings and update repository
            LaunchedEffect(Unit) {
                settingsManager.members.collect { membersList ->
                    piketRepository.updateMembers(membersList)
                }
            }

            PiketersTheme {
                AppNavigation(
                    settingsManager = settingsManager,
                    piketRepository = piketRepository
                )
            }
        }
    }
}
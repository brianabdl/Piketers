package com.brianabdl.piketers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brianabdl.piketers.data.models.PiketAssignment
import com.brianabdl.piketers.data.preferences.SettingsManager
import com.brianabdl.piketers.data.repository.PiketRepository
import com.brianabdl.piketers.utils.TelegramService
import com.brianabdl.piketers.utils.WhatsappService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    piketRepository: PiketRepository,
    settingsManager: SettingsManager,
    telegramService: TelegramService,
    navigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val currentAssignment by piketRepository.currentAssignment.collectAsState()
    val currentDay = piketRepository.getCurrentDayName()
    val coroutineScope = rememberCoroutineScope()

    var isSending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showSnackbar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        // Generate today's assignment when the screen is first displayed
        piketRepository.generateTodayPiketAssignment()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Piket Scheduler") },
                actions = {
                    IconButton(onClick = navigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Day Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hari Ini",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentDay,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Piket Assignment Display
            currentAssignment?.let { assignment ->
                PiketAssignmentCard(assignment)
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada jadwal piket hari ini",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        piketRepository.generateTodayPiketAssignment()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regenerate")
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            currentAssignment?.let { assignment ->
                                statusMessage = null
                                showSnackbar = false
                                try {
                                    val result = WhatsappService.sendMessage(
                                        context = context,
                                        text = assignment.message
                                    )

                                    result.fold(
                                        onSuccess = { success ->
                                            statusMessage = success
                                            showSnackbar = true
                                        },
                                        onFailure = { error ->
                                            statusMessage = "Error: ${error.message}"
                                            showSnackbar = true
                                        }
                                    )
                                } catch (e: Exception) {
                                    statusMessage = "Error: ${e.message}"
                                    showSnackbar = true
                                }
                            }
                        }
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send to WhatsApp")
                }
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        currentAssignment?.let { assignment ->
                            isSending = true
                            statusMessage = null

                            try {
                                val botToken = settingsManager.botToken.first()
                                val chatId = settingsManager.targetChatId.first()

                                if (botToken.isBlank()) {
                                    statusMessage =
                                        "Bot token tidak ditemukan. Silakan masukkan di pengaturan."
                                    showSnackbar = true
                                    return@launch
                                }

                                if (chatId.isBlank()) {
                                    statusMessage =
                                        "Chat ID tidak ditemukan. Silakan masukkan di pengaturan."
                                    showSnackbar = true
                                    return@launch
                                }

                                val result = telegramService.sendMessage(
                                    botToken = botToken,
                                    chatId = chatId,
                                    text = assignment.message
                                )

                                result.fold(
                                    onSuccess = { success ->
                                        statusMessage = success
                                        showSnackbar = true
                                    },
                                    onFailure = { error ->
                                        statusMessage = "Error: ${error.message}"
                                        showSnackbar = true
                                    }
                                )
                            } finally {
                                isSending = false
                            }
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send to Telegram")
            }

            if (showSnackbar && statusMessage != null) {
                LaunchedEffect(statusMessage) {
                    snackbarHostState.showSnackbar(message = statusMessage ?: "")
                    showSnackbar = false
                }
            }
        }
    }
}

@Composable
fun PiketAssignmentCard(assignment: PiketAssignment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "JADWAL PIKET ${assignment.taskType.uppercase()}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                when (assignment.taskType) {
                    "jendela" -> {
                        item {
                            Text(
                                text = "Lantai 1",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Dalam: ${assignment.assignments[0]}")
                            Text("Luar: ${assignment.assignments[1]}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Text(
                                text = "Lantai 2",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Dalam: ${assignment.assignments[2]}")
                            Text("Luar: ${assignment.assignments[3]}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Text(
                                text = "Lantai 3",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Dalam: ${assignment.assignments[4]}")
                            Text("Luar: ${assignment.assignments[5]}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (assignment.assignments.size > 6) {
                            item {
                                Text(
                                    text = "Sekat Besar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(assignment.assignments[6])
                            }
                        }
                    }

                    "tangga" -> {
                        item {
                            Text(
                                text = "Laki-Laki",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Lantai 1-2: ${assignment.assignments[0]}")
                            Text("Lantai 3-4: ${assignment.assignments[1]}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Text(
                                text = "Perempuan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Lantai 1-2: ${assignment.assignments[2]}")
                            Text("Lantai 3-4: ${assignment.assignments[3]}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Text(
                                text = "Belakang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Lantai 1-2: ${assignment.assignments[4]}")
                            Text("Lantai 3-4: ${assignment.assignments[5]}")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (assignment.assignments.size > 6) {
                            item {
                                Text(
                                    text = "Sekat Besar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(assignment.assignments[6])
                            }
                        }
                    }

                    else -> {
                        item {
                            Text(
                                text = "Kuy, piket ${assignment.taskType}",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brianabdl.piketers.data.models.PiketAssignment
import com.brianabdl.piketers.data.repository.PiketRepository
import com.brianabdl.piketers.utils.WhatsappService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    piketRepository: PiketRepository,
    navigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val currentAssignment by piketRepository.currentAssignment.collectAsState()
    val currentDay = piketRepository.getCurrentDayName()
    val coroutineScope = rememberCoroutineScope()

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
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send to WhatsApp")
                }
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
            val annotatedString = buildAnnotatedString {
                appendStyledText(assignment.message)
            }
            Text(
                text = annotatedString,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


fun AnnotatedString.Builder.appendStyledText(text: String) {
    // Define regex patterns
    val patterns = listOf(
        Pair("\\*(.*?)\\*", SpanStyle(fontWeight = FontWeight.Bold)),      // *bold*
        Pair("_(.*?)_", SpanStyle(fontStyle = FontStyle.Italic)),          // _italic_
        Pair("~(.*?)~", SpanStyle(textDecoration = TextDecoration.LineThrough)), // ~strike~
        Pair("`(.*?)`", SpanStyle(fontFamily = FontFamily.Monospace, color = Color(0xFF2E7D32))) // `monospace`
    )

    // Keep track of remaining text
    var remaining = text
    var lastIndex = 0

    while (remaining.isNotEmpty()) {
        var earliestMatch: MatchResult? = null
        var earliestPattern: Pair<String, SpanStyle>? = null

        // Find the earliest match across all patterns
        for (pattern in patterns) {
            val regex = Regex(pattern.first)
            val match = regex.find(remaining)
            if (match != null && (earliestMatch == null || match.range.first < earliestMatch.range.first)) {
                earliestMatch = match
                earliestPattern = pattern
            }
        }

        if (earliestMatch != null && earliestPattern != null) {
            // Append text before the match (normal style)
            append(remaining.substring(0, earliestMatch.range.first))

            // Apply span to matched content (without markers)
            pushStyle(earliestPattern.second)
            append(earliestMatch.groupValues[1])
            pop()

            // Move past this match
            remaining = remaining.substring(earliestMatch.range.last + 1)
        } else {
            // No more matches, append the rest
            append(remaining)
            break
        }
    }
}
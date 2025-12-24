package com.dabi.easylocalgame_kmp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dabi.easylocalgame_kmp.data.ChatMessage
import com.dabi.easylocalgame_kmp.viewmodel.DemoServerViewModel
import com.dabi.easylocalgamekmplibrary.server.ServerStatusEnum

/**
 * Server screen - Start a game server and manage connections.
 * 
 * Uses DemoServerViewModel which extends ServerViewModelTemplate.
 */
@Composable
fun ServerScreen(
    viewModel: DemoServerViewModel,
    onBack: () -> Unit
) {
    // Use serverState from the template (via serverManager.serverState)
    val serverState by viewModel.serverState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    
    var serverName by remember { mutableStateOf("GameServer") }
    var customMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎮 Server Mode",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = {
                viewModel.stopServer()
                onBack()
            }) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Card
        StatusCard(
            status = serverState.serverStatus.name,
            isActive = serverState.serverStatus == ServerStatusEnum.ADVERTISING || 
                       serverState.serverStatus == ServerStatusEnum.ACTIVE,
            isError = serverState.serverStatus == ServerStatusEnum.ADVERTISING_FAILED,
            details = "Connected: ${serverState.connectedClients.size} | " +
                     "Players: ${gameState.players.joinToString { it.name }.ifEmpty { "none" }}"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Controls
        when (serverState.serverStatus) {
            ServerStatusEnum.NONE, ServerStatusEnum.CLOSED, ServerStatusEnum.ADVERTISING_FAILED -> {
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("Server Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.startServer(serverName) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Server")
                }
            }
            
            ServerStatusEnum.ADVERTISING, ServerStatusEnum.ACTIVE -> {
                MessageControls(
                    message = customMessage,
                    onMessageChange = { customMessage = it },
                    onSend = {
                        if (customMessage.isNotBlank()) {
                            viewModel.sendMessage(customMessage)
                            customMessage = ""
                        }
                    },
                    onSendRandom = { viewModel.sendRandomMessage() },
                    onStop = { viewModel.stopServer() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Messages (${gameState.messages.size})", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        MessageList(messages = gameState.messages, modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatusCard(
    status: String,
    isActive: Boolean,
    isError: Boolean,
    details: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isActive -> MaterialTheme.colorScheme.primaryContainer
                isError -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Status: $status", style = MaterialTheme.typography.titleMedium)
            Text(text = details, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MessageControls(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onSendRandom: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            label = { Text("Message") },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onSend) { Text("Send") }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onSendRandom, modifier = Modifier.weight(1f)) {
            Text("Send Random 🎲")
        }
        OutlinedButton(
            onClick = onStop,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Stop")
        }
    }
}

@Composable
fun MessageList(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No messages yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(messages) { message ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(message.senderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(message.message, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

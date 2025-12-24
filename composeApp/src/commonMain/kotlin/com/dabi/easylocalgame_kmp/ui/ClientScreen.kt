package com.dabi.easylocalgame_kmp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dabi.easylocalgame_kmp.viewmodel.DemoClientViewModel
import com.dabi.easylocalgamekmplibrary.client.ConnectionStatusEnum

/**
 * Client screen - Connect to a game server and play.
 * 
 * Uses DemoClientViewModel which extends PlayerViewModelTemplate.
 */
@Composable
fun ClientScreen(
    viewModel: DemoClientViewModel,
    onBack: () -> Unit
) {
    // Use clientState from the template (via clientManager.clientState)
    val clientState by viewModel.clientState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    
    var playerName by remember { mutableStateOf("Player") }
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
                text = "🎯 Client Mode",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = {
                viewModel.disconnect()
                onBack()
            }) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Card
        StatusCard(
            status = clientState.connectionStatus.name,
            isActive = clientState.connectionStatus == ConnectionStatusEnum.CONNECTED ||
                       clientState.connectionStatus == ConnectionStatusEnum.CONNECTION_ESTABLISHED,
            isError = clientState.connectionStatus == ConnectionStatusEnum.CONNECTING_FAILED ||
                     clientState.connectionStatus == ConnectionStatusEnum.CONNECTING_REJECTED ||
                     clientState.connectionStatus == ConnectionStatusEnum.ROOM_IS_FULL,
            details = if (clientState.serverID.isNotEmpty()) 
                "Server: ${clientState.serverID.take(8)}... | Players: ${gameState.players.size}"
                else "Looking for servers..."
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Controls
        when (clientState.connectionStatus) {
            ConnectionStatusEnum.NONE, ConnectionStatusEnum.DISCONNECTED,
            ConnectionStatusEnum.CONNECTING_FAILED, ConnectionStatusEnum.CONNECTING_REJECTED,
            ConnectionStatusEnum.ROOM_IS_FULL, ConnectionStatusEnum.ENDPOINT_LOST -> {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Your Nickname") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.connect(playerName) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connect to Server")
                }
                
                if (clientState.connectionStatus == ConnectionStatusEnum.ROOM_IS_FULL) {
                    Text("⚠️ Server room is full", color = MaterialTheme.colorScheme.error)
                }
            }
            
            ConnectionStatusEnum.CONNECTING -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Looking for servers...")
                    }
                }
            }
            
            ConnectionStatusEnum.CONNECTED, ConnectionStatusEnum.CONNECTION_ESTABLISHED -> {
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
                    onStop = { viewModel.disconnect() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Messages (${messages.size})", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        MessageList(messages = messages, modifier = Modifier.weight(1f))
    }
}

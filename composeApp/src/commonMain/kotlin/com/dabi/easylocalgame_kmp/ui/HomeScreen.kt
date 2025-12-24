package com.dabi.easylocalgame_kmp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Home screen - Choose to be server or client.
 */
@Composable
fun HomeScreen(
    onServerClick: () -> Unit,
    onClientClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎮 EasyLocalGame",
            style = MaterialTheme.typography.displaySmall
        )
        
        Text(
            text = "KMP Demo App",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose your role",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onServerClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🖥️  Host as Server")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = onClientClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📱  Join as Client")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📝 How it works",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Server starts advertising and waits for clients\n" +
                           "• Clients discover nearby servers and connect\n" +
                           "• Messages are sent using custom payload types\n" +
                           "• Uses Google Nearby Connections API",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

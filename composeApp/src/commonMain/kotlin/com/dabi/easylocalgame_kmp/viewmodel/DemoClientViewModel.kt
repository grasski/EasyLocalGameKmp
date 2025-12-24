package com.dabi.easylocalgame_kmp.viewmodel

import com.dabi.easylocalgame_kmp.data.ChatMessage
import com.dabi.easylocalgame_kmp.data.DemoClientAction
import com.dabi.easylocalgame_kmp.data.DemoGameState
import com.dabi.easylocalgamekmplibrary.actions.ServerAction
import com.dabi.easylocalgamekmplibrary.client.PlayerConnectionState
import com.dabi.easylocalgamekmplibrary.connection.NearbyConnectionsManager
import com.dabi.easylocalgamekmplibrary.payload.fromServerPayload
import com.dabi.easylocalgamekmplibrary.payload.toPayload
import com.dabi.easylocalgamekmplibrary.viewmodel.PlayerViewModelTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock

/**
 * Demo Client ViewModel - extends PlayerViewModelTemplate from library.
 * 
 * Shows how to:
 * - Extend the library's template
 * - Use library's payload types (ClientPayloadType, ServerPayloadType)
 * - Handle server actions
 */
class DemoClientViewModel(
    connectionsManager: NearbyConnectionsManager
) : PlayerViewModelTemplate(connectionsManager) {
    
    // Game state received from server
    private val _gameState = MutableStateFlow(DemoGameState())
    val gameState: StateFlow<DemoGameState> = _gameState.asStateFlow()
    
    // Convenience property to expose client state from parent template
    val clientState get() = clientManager.clientState
    
    // Messages for UI
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    // Player nickname
    private var nickname: String = "Player"
    
    /**
     * Connect to a server.
     */
    fun connect(playerName: String) {
        nickname = playerName
        
        // Use library's connect - it sends ESTABLISH_CONNECTION automatically
        clientManager.connect(
            packageName = DemoServerViewModel.SERVICE_ID,
            playerConnectionState = PlayerConnectionState(
                nickname = playerName,
                avatarId = (1..10).random()
            )
        )
    }
    
    /**
     * Disconnect from server.
     */
    fun disconnect() {
        clientManager.disconnect()
        _gameState.update { DemoGameState() }
        _messages.update { emptyList() }
    }
    
    /**
     * Mark player as ready.
     * Uses custom client action.
     */
    fun setReady() {
        // Send custom action as raw JSON
        val customPayload = """{"type":"READY"}""".encodeToByteArray()
        clientManager.sendPayload(customPayload)
    }
    
    /**
     * Send a chat message to the server.
     * The server will broadcast it back to all clients (including sender).
     */
    fun sendMessage(message: String) {
        // Send to server - server will broadcast to all clients
        val payload = toPayload(DemoClientAction.SEND_MESSAGE.name, ChatMessage(nickname, message))
        clientManager.sendPayload(payload)
    }
    
    /**
     * Send random test message.
     */
    fun sendRandomMessage() {
        val messages = listOf(
            "Hello from client! 👋",
            "Random: ${(1000..9999).random()}",
            "Client ping"
        )
        sendMessage(messages.random())
    }
    
    /**
     * Override from PlayerViewModelTemplate - handle server actions.
     */
    override fun serverAction(serverAction: ServerAction) {
        when (serverAction) {
            is ServerAction.UpdateGameState -> {
                // Parse game state using library function
                val (_, gameState) = fromServerPayload<DemoGameState>(serverAction.payload)
                gameState?.let { state ->
                    _gameState.update { state }
                    // Update local messages from game state
                    _messages.update { state.messages }
                }
            }
            
            is ServerAction.UpdatePlayerState -> {
                // Parse player-specific state updates
            }
            
            is ServerAction.PayloadAction -> {
                // Handle custom server actions if needed
            }
        }
    }
}

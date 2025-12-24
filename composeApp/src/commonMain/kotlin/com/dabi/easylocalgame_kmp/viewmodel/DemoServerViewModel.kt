package com.dabi.easylocalgame_kmp.viewmodel

import com.dabi.easylocalgame_kmp.data.ChatMessage
import com.dabi.easylocalgame_kmp.data.DemoClientAction
import com.dabi.easylocalgame_kmp.data.DemoGameState
import com.dabi.easylocalgame_kmp.data.DemoPlayerInfo
import com.dabi.easylocalgamekmplibrary.actions.ClientAction
import com.dabi.easylocalgamekmplibrary.client.PlayerConnectionState
import com.dabi.easylocalgamekmplibrary.connection.NearbyConnectionsManager
import com.dabi.easylocalgamekmplibrary.payload.ServerPayloadType
import com.dabi.easylocalgamekmplibrary.payload.fromClientPayload
import com.dabi.easylocalgamekmplibrary.payload.fromPayload
import com.dabi.easylocalgamekmplibrary.payload.getPayloadType
import com.dabi.easylocalgamekmplibrary.payload.toServerPayload
import com.dabi.easylocalgamekmplibrary.server.ServerConfiguration
import com.dabi.easylocalgamekmplibrary.server.ServerType
import com.dabi.easylocalgamekmplibrary.viewmodel.ServerViewModelTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Demo Server ViewModel - extends ServerViewModelTemplate from library.
 * 
 * Shows how to:
 * - Extend the library's template
 * - Use library's payload types (ServerPayloadType.UPDATE_GAME_STATE, etc.)
 * - Handle custom client actions via PayloadAction
 */
class DemoServerViewModel(
    connectionsManager: NearbyConnectionsManager
) : ServerViewModelTemplate(connectionsManager) {
    
    // Your app-specific game state
    private val _gameState = MutableStateFlow(DemoGameState())
    val gameState: StateFlow<DemoGameState> = _gameState.asStateFlow()
    
    // Convenience property to expose server state from parent template
    val serverState get() = serverManager.serverState

    // Player registry (endpointId -> playerInfo)
    private val playerRegistry = mutableMapOf<String, DemoPlayerInfo>()
    
    /**
     * Start the server.
     */
    fun startServer(serverName: String) {
        serverManager.startServer(
            packageName = SERVICE_ID,
            configuration = ServerConfiguration(
                serverType = ServerType.IS_TABLE,
                maximumConnections = 8,
                serverAsPlayerName = serverName
            )
        )
    }
    
    /**
     * Stop the server.
     */
    fun stopServer() {
        serverManager.closeServer()
        playerRegistry.clear()
        _gameState.update { DemoGameState() }
    }
    
    /**
     * Send a chat message to all clients.
     * Uses library's toServerPayload with custom data.
     */
    fun sendMessage(message: String) {
        val chatMessage = ChatMessage(senderName = "Server", message = message)

        // Send using library's UPDATE_GAME_STATE type
        // (You could also create a custom payload type for messages)
        _gameState.update { 
            it.copy(
                messageCount = it.messageCount + 1,
                lastMessage = message,
                messages = it.messages + chatMessage
            )
        }
        
        // Broadcast game state to all clients
        val payload = toServerPayload(ServerPayloadType.UPDATE_GAME_STATE, _gameState.value)
        serverManager.sendPayload(payload)
    }
    
    /**
     * Send random test message.
     */
    fun sendRandomMessage() {
        val messages = listOf(
            "Hello from server! 👋",
            "Random: ${(1000..9999).random()}",
            "Server ping at ${System.currentTimeMillis()}"
        )
        sendMessage(messages.random())
    }
    
    /**
     * Override from ServerViewModelTemplate - handle client actions.
     */
    override fun clientAction(clientAction: ClientAction) {
        when (clientAction) {
            is ClientAction.EstablishConnection -> {
                // Parse player info using library function
                val (_, playerInfo) = fromClientPayload<PlayerConnectionState>(clientAction.payload)
                playerInfo?.let {
                    val player = DemoPlayerInfo(
                        id = clientAction.endpointID,
                        name = it.nickname
                    )
                    playerRegistry[clientAction.endpointID] = player
                    broadcastPlayerList()
                    
                    addSystemMessage("${it.nickname} joined the game!")
                }
            }
            
            is ClientAction.Disconnect -> {
                val player = playerRegistry.remove(clientAction.endpointID)
                player?.let {
                    broadcastPlayerList()
                    addSystemMessage("${it.name} left the game")
                }
            }
            
            is ClientAction.PayloadAction -> {
                // Handle custom client actions
                handleCustomAction(clientAction.endpointID, clientAction.payload)
            }
        }
    }
    
    /**
     * Handle your custom client action types.
     */
    private fun handleCustomAction(clientId: String, payload: ByteArray) {
        try {
            // Get the type string without deserializing data
            // (fromPayload<Any?> doesn't work with kotlinx.serialization)
            val actionType = getPayloadType(payload)
            
            // Try to match to our custom action enum
            val action = try {
                DemoClientAction.valueOf(actionType)
            } catch (e: IllegalArgumentException) {
                // Unknown action type - might be a library internal type, ignore
                return
            }
            
            when (action) {
                DemoClientAction.READY -> {
                    playerRegistry[clientId]?.let { player ->
                        playerRegistry[clientId] = player.copy(isReady = true)
                        addSystemMessage("${player.name} is ready!")
                        broadcastGameState()
                    }
                }
                DemoClientAction.SEND_MESSAGE -> {
                    // Parse with the correct data type
                    val (_, chatData) = fromPayload<ChatMessage>(payload)
                    chatData?.let { message ->
                        val playerName = playerRegistry[clientId]?.name ?: "Unknown"
                        val updatedMessage = message.copy(senderName = playerName)
                        
                        // Add to game state and broadcast to all clients
                        _gameState.update { state ->
                            state.copy(
                                messageCount = state.messageCount + 1,
                                lastMessage = message.message,
                                messages = state.messages + updatedMessage
                            )
                        }

                        broadcastGameState()
                    }
                }
                DemoClientAction.REQUEST_STATE -> {
                    // Send current game state to the requesting client
                    val statePayload = toServerPayload(ServerPayloadType.UPDATE_GAME_STATE, _gameState.value)
                    serverManager.sendPayload(clientId, statePayload)
                }
            }
        } catch (e: Exception) {
            // Failed to parse - ignore unknown payloads
        }
    }
    
    private fun addSystemMessage(text: String) {
        val sysMessage = ChatMessage("System", text)
        // Also add to game state so clients see it
        _gameState.update { state ->
            state.copy(messages = state.messages + sysMessage)
        }
    }
    
    private fun broadcastGameState() {
        // Update players list
        _gameState.update { 
            it.copy(players = playerRegistry.values.toList())
        }
        val payload = toServerPayload(ServerPayloadType.UPDATE_GAME_STATE, _gameState.value)
        serverManager.sendPayload(payload)
    }
    
    private fun broadcastPlayerList() {
        broadcastGameState()  // Just use the same function
    }
    
    companion object {
        const val SERVICE_ID = "com.dabi.easylocalgame_kmp"
    }
}

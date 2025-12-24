package com.dabi.easylocalgamekmplibrary.viewmodel

import androidx.lifecycle.ViewModel
import com.dabi.easylocalgamekmplibrary.actions.ClientAction
import com.dabi.easylocalgamekmplibrary.actions.ServerAction
import com.dabi.easylocalgamekmplibrary.client.ClientManager
import com.dabi.easylocalgamekmplibrary.connection.NearbyConnectionsManager
import com.dabi.easylocalgamekmplibrary.server.ServerManager

/**
 * Abstract ViewModel template for server-side game logic.
 *
 * Extend this class to create your game server ViewModel.
 * Use [serverManager] to handle client connections and send payloads.
 *
 * Example:
 * ```kotlin
 * class MyServerViewModel(connectionsManager: NearbyConnectionsManager) :
 *     ServerViewModelTemplate(connectionsManager) {
 *
 *     private val _gameState = MutableStateFlow(MyGameState())
 *     val gameState = _gameState.asStateFlow()
 *
 *     override fun clientAction(clientAction: ClientAction) {
 *         when (clientAction) {
 *             is ClientAction.EstablishConnection -> registerPlayer(clientAction)
 *             is ClientAction.Disconnect -> removePlayer(clientAction.endpointID)
 *             is ClientAction.PayloadAction -> handleCustomAction(clientAction)
 *         }
 *     }
 * }
 * ```
 */
abstract class ServerViewModelTemplate(
    connectionsManager: NearbyConnectionsManager
) : ViewModel() {

    /**
     * Override this function to handle client actions in your game state.
     *
     * [ClientAction.PayloadAction] is used for custom actions not handled by the library.
     * [ClientAction.EstablishConnection] is sent when a client first connects.
     * [ClientAction.Disconnect] is sent when a client disconnects.
     */
    abstract fun clientAction(clientAction: ClientAction)

    /**
     * The server manager instance for handling connections and payloads.
     */
    val serverManager: ServerManager by lazy {
        ServerManager(connectionsManager, ::clientAction)
    }
}

/**
 * Abstract ViewModel template for client-side game logic.
 *
 * Extend this class to create your player ViewModel.
 * Use [clientManager] to handle server connection and send payloads.
 *
 * Example:
 * ```kotlin
 * class MyPlayerViewModel(connectionsManager: NearbyConnectionsManager) :
 *     PlayerViewModelTemplate(connectionsManager) {
 *
 *     private val _playerState = MutableStateFlow(MyPlayerState())
 *     val playerState = _playerState.asStateFlow()
 *
 *     override fun serverAction(serverAction: ServerAction) {
 *         when (serverAction) {
 *             is ServerAction.UpdateGameState -> updateGame(serverAction.payload)
 *             is ServerAction.UpdatePlayerState -> updatePlayer(serverAction.payload)
 *             is ServerAction.PayloadAction -> handleCustomAction(serverAction)
 *         }
 *     }
 * }
 * ```
 */
abstract class PlayerViewModelTemplate(
    connectionsManager: NearbyConnectionsManager
) : ViewModel() {

    /**
     * Override this function to handle server actions in your game state.
     *
     * [ServerAction.PayloadAction] is used for custom actions not handled by the library.
     * [ServerAction.UpdateGameState] is sent when the server updates the game state.
     * [ServerAction.UpdatePlayerState] is sent when the server updates this player's state.
     */
    abstract fun serverAction(serverAction: ServerAction)

    /**
     * The client manager instance for handling connection and payloads.
     */
    val clientManager: ClientManager by lazy {
        ClientManager(connectionsManager, ::serverAction)
    }
}

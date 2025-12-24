package com.dabi.easylocalgamekmplibrary.client

import com.dabi.easylocalgamekmplibrary.actions.ServerAction
import com.dabi.easylocalgamekmplibrary.connection.ConnectionCallbacks
import com.dabi.easylocalgamekmplibrary.connection.ConnectionStatusCodes
import com.dabi.easylocalgamekmplibrary.connection.ConnectionStrategy
import com.dabi.easylocalgamekmplibrary.connection.DiscoveryCallbacks
import com.dabi.easylocalgamekmplibrary.connection.NearbyConnectionsManager
import com.dabi.easylocalgamekmplibrary.connection.PayloadCallbacks
import com.dabi.easylocalgamekmplibrary.logging.EasyLocalGameLogger
import com.dabi.easylocalgamekmplibrary.payload.ClientPayloadType
import com.dabi.easylocalgamekmplibrary.payload.ServerPayloadType
import com.dabi.easylocalgamekmplibrary.payload.fromPayload
import com.dabi.easylocalgamekmplibrary.payload.getPayloadType
import com.dabi.easylocalgamekmplibrary.payload.toClientPayload
import com.dabi.easylocalgamekmplibrary.server.ServerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages client-side operations for local multiplayer games.
 * Handles discovery, server connection, and payload communication.
 *
 * @param connectionsManager Platform-specific Nearby Connections implementation
 * @param serverAction Callback to handle server actions in your game logic
 */
class ClientManager(
    private val connectionsManager: NearbyConnectionsManager,
    private val serverAction: (ServerAction) -> Unit
) {
    private val _clientState = MutableStateFlow(ClientState())
    
    /** Observable client state. */
    val clientState: StateFlow<ClientState> = _clientState.asStateFlow()

    private lateinit var playerConnectionState: IPlayerConnectionState
    private lateinit var payloadCallbacks: PayloadCallbacks

    companion object {
        private const val TAG = "ClientManager"
    }

    /**
     * Start connecting to a server.
     *
     * @param packageName Service ID (usually app package name)
     * @param playerConnectionState Player info to send to server
     */
    fun connect(packageName: String, playerConnectionState: IPlayerConnectionState) {
        EasyLocalGameLogger.i("Connecting as '${playerConnectionState.nickname}' to service: $packageName", TAG)
        
        if (_clientState.value.connectionStatus == ConnectionStatusEnum.CONNECTING ||
            _clientState.value.connectionStatus == ConnectionStatusEnum.CONNECTED ||
            _clientState.value.connectionStatus == ConnectionStatusEnum.CONNECTION_ESTABLISHED
        ) {
            EasyLocalGameLogger.w("Already connecting/connected, ignoring connect request. Status: ${_clientState.value.connectionStatus}", TAG)
            return
        }

        this.playerConnectionState = playerConnectionState
        startDiscovery(packageName, playerConnectionState.nickname)
    }

    /**
     * Disconnect from the server.
     */
    fun disconnect() {
        EasyLocalGameLogger.i("Disconnecting from server: ${_clientState.value.serverID}", TAG)
        
        _clientState.update { it.copy(connectionStatus = ConnectionStatusEnum.DISCONNECTED) }

        connectionsManager.disconnectFromEndpoint(_clientState.value.serverID)
        connectionsManager.stopDiscovery()
        connectionsManager.stopAllEndpoints()
        
        EasyLocalGameLogger.i("Disconnected successfully", TAG)
    }

    /**
     * Send a payload to the server.
     *
     * @param payload The data to send
     */
    fun sendPayload(payload: ByteArray) {
        EasyLocalGameLogger.d("Sending payload to server (${payload.size} bytes)", TAG)
        connectionsManager.sendPayload(_clientState.value.serverID, payload)
    }

    private fun establishConnection() {
        EasyLocalGameLogger.i("Establishing connection as '${playerConnectionState.nickname}'", TAG)
        
        // Send player info to server
        val connectionPayload = toClientPayload(
            ClientPayloadType.ESTABLISH_CONNECTION,
            PlayerConnectionState(
                nickname = playerConnectionState.nickname,
                avatarId = playerConnectionState.avatarId
            )
        )
        sendPayload(connectionPayload)

        _clientState.update {
            it.copy(connectionStatus = ConnectionStatusEnum.CONNECTION_ESTABLISHED)
        }

        connectionsManager.stopDiscovery()
        EasyLocalGameLogger.i("✅ Connection established with server", TAG)
    }

    private fun startDiscovery(packageName: String, nickname: String) {
        EasyLocalGameLogger.d("Starting discovery for service: $packageName", TAG)
        
        connectionsManager.stopDiscovery()

        _clientState.update {
            it.copy(connectionStatus = ConnectionStatusEnum.NONE)
        }

        payloadCallbacks = createPayloadCallbacks()

        connectionsManager.startDiscovery(
            serviceId = packageName,
            strategy = ConnectionStrategy.P2P_STAR,
            discoveryCallbacks = createDiscoveryCallbacks(nickname),
            onSuccess = {
                EasyLocalGameLogger.i("🔍 Discovery started, looking for servers...", TAG)
                _clientState.update {
                    it.copy(connectionStatus = ConnectionStatusEnum.CONNECTING)
                }
            },
            onFailure = { exception ->
                EasyLocalGameLogger.e("❌ Discovery failed: ${exception.message}", TAG, exception)
                _clientState.update {
                    it.copy(connectionStatus = ConnectionStatusEnum.CONNECTING_FAILED)
                }
            }
        )
    }

    private fun createDiscoveryCallbacks(nickname: String) = object : DiscoveryCallbacks {
        override fun onEndpointFound(endpointId: String, endpointName: String, serviceId: String) {
            EasyLocalGameLogger.i("🎯 Server found: '$endpointName' ($endpointId)", TAG)
            
            // Auto-connect to first found server
            EasyLocalGameLogger.d("Requesting connection to '$endpointName'", TAG)
            connectionsManager.requestConnection(
                name = nickname,
                endpointId = endpointId,
                connectionCallbacks = connectionCallbacks,
                payloadCallbacks = payloadCallbacks,
                onSuccess = { 
                    EasyLocalGameLogger.d("Connection request sent to $endpointId", TAG)
                },
                onFailure = { exception ->
                    EasyLocalGameLogger.e("❌ Connection request failed: ${exception.message}", TAG, exception)
                    _clientState.update {
                        it.copy(connectionStatus = ConnectionStatusEnum.CONNECTING_FAILED)
                    }
                }
            )
        }

        override fun onEndpointLost(endpointId: String) {
            EasyLocalGameLogger.w("Server endpoint lost: $endpointId", TAG)
            _clientState.update {
                it.copy(connectionStatus = ConnectionStatusEnum.ENDPOINT_LOST)
            }
        }
    }

    private val connectionCallbacks = object : ConnectionCallbacks {
        override fun onConnectionInitiated(endpointId: String, endpointName: String) {
            EasyLocalGameLogger.d("Connection initiated with server '$endpointName' ($endpointId)", TAG)
            connectionsManager.acceptConnection(endpointId, payloadCallbacks)
        }

        override fun onConnectionResult(endpointId: String, isSuccess: Boolean, statusCode: Int) {
            when (statusCode) {
                ConnectionStatusCodes.STATUS_OK -> {
                    EasyLocalGameLogger.i("✅ Connected to server: $endpointId", TAG)
                    _clientState.update {
                        it.copy(
                            serverID = endpointId,
                            connectionStatus = ConnectionStatusEnum.CONNECTED
                        )
                    }
                    establishConnection()
                }
                ConnectionStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    EasyLocalGameLogger.w("Connection rejected by server: $endpointId", TAG)
                    _clientState.update {
                        it.copy(connectionStatus = ConnectionStatusEnum.CONNECTING_REJECTED)
                    }
                }
                else -> {
                    EasyLocalGameLogger.e("Connection failed with statusCode: $statusCode", TAG)
                    _clientState.update {
                        it.copy(connectionStatus = ConnectionStatusEnum.CONNECTING_FAILED)
                    }
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            EasyLocalGameLogger.w("👋 Disconnected from server: $endpointId", TAG)
            _clientState.update {
                it.copy(connectionStatus = ConnectionStatusEnum.DISCONNECTED)
            }
        }
    }

    private fun createPayloadCallbacks() = object : PayloadCallbacks {
        override fun onPayloadReceived(endpointId: String, payload: ByteArray) {
            EasyLocalGameLogger.d("Payload received from server (${payload.size} bytes)", TAG)
            
            // Use getPayloadType to extract type without deserializing data
            // (avoids issues with complex type serialization)
            val typeStr = getPayloadType(payload)
            val serverPayloadType = try {
                ServerPayloadType.valueOf(typeStr)
            } catch (e: IllegalArgumentException) {
                null
            }
            
            EasyLocalGameLogger.d("Payload type: $typeStr (library type: $serverPayloadType)", TAG)

            when (serverPayloadType) {
                ServerPayloadType.CLIENT_CONNECTED -> {
                    // For CLIENT_CONNECTED, data is expected to be a String (ServerType name)
                    val (_, data) = fromPayload<String?>(payload)
                    val serverType = try {
                        ServerType.valueOf(data ?: "IS_TABLE")
                    } catch (e: IllegalArgumentException) {
                        ServerType.IS_TABLE
                    }
                    EasyLocalGameLogger.i("Server confirmed connection. Type: $serverType", TAG)
                    _clientState.update {
                        it.copy(serverType = serverType)
                    }
                }
                ServerPayloadType.ROOM_IS_FULL -> {
                    EasyLocalGameLogger.w("Server room is full, cannot join", TAG)
                    _clientState.update {
                        it.copy(connectionStatus = ConnectionStatusEnum.ROOM_IS_FULL)
                    }
                }
                ServerPayloadType.UPDATE_PLAYER_STATE -> {
                    EasyLocalGameLogger.d("Received player state update", TAG)
                    serverAction(ServerAction.UpdatePlayerState(payload))
                }
                ServerPayloadType.UPDATE_GAME_STATE -> {
                    EasyLocalGameLogger.d("Received game state update", TAG)
                    serverAction(ServerAction.UpdateGameState(payload))
                }
                null -> {
                    EasyLocalGameLogger.d("Received custom payload", TAG)
                    serverAction(ServerAction.PayloadAction(payload))
                }
            }
        }

        override fun onPayloadTransferUpdate(
            endpointId: String,
            payloadId: Long,
            bytesTransferred: Long,
            totalBytes: Long,
            isSuccess: Boolean?
        ) {
            when (isSuccess) {
                true -> EasyLocalGameLogger.d("Payload $payloadId transfer complete", TAG)
                false -> EasyLocalGameLogger.w("Payload $payloadId transfer failed", TAG)
                null -> {} // In progress, don't log to avoid spam
            }
        }
    }
}

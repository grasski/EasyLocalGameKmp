package com.dabi.easylocalgamekmplibrary.server

import com.dabi.easylocalgamekmplibrary.actions.ClientAction
import com.dabi.easylocalgamekmplibrary.connection.ConnectionCallbacks
import com.dabi.easylocalgamekmplibrary.connection.ConnectionStatusCodes
import com.dabi.easylocalgamekmplibrary.connection.ConnectionStrategy
import com.dabi.easylocalgamekmplibrary.connection.NearbyConnectionsManager
import com.dabi.easylocalgamekmplibrary.connection.PayloadCallbacks
import com.dabi.easylocalgamekmplibrary.logging.EasyLocalGameLogger
import com.dabi.easylocalgamekmplibrary.payload.ClientPayloadType
import com.dabi.easylocalgamekmplibrary.payload.ServerPayloadType
import com.dabi.easylocalgamekmplibrary.payload.fromClientPayload
import com.dabi.easylocalgamekmplibrary.payload.toServerPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages server-side operations for local multiplayer games.
 * Handles advertising, client connections, and payload communication.
 *
 * @param connectionsManager Platform-specific Nearby Connections implementation
 * @param clientAction Callback to handle client actions in your game logic
 */
class ServerManager(
    private val connectionsManager: NearbyConnectionsManager,
    private val clientAction: (ClientAction) -> Unit
) {
    private val _serverState = MutableStateFlow(ServerState())
    
    /** Observable server state. */
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()

    private lateinit var serverConfiguration: ServerConfiguration
    private lateinit var payloadCallbacks: PayloadCallbacks

    companion object {
        private const val TAG = "ServerManager"
    }

    /**
     * Start the server and begin advertising.
     *
     * @param packageName Service ID (usually app package name)
     * @param configuration Server configuration options
     */
    fun startServer(packageName: String, configuration: ServerConfiguration) {
        EasyLocalGameLogger.i("Starting server with package: $packageName, type: ${configuration.serverType}, maxConnections: ${configuration.maximumConnections}", TAG)
        
        if (_serverState.value.serverStatus == ServerStatusEnum.ADVERTISING ||
            _serverState.value.serverStatus == ServerStatusEnum.ACTIVE
        ) {
            EasyLocalGameLogger.w("Server already running, ignoring start request. Current status: ${_serverState.value.serverStatus}", TAG)
            return
        }

        this.serverConfiguration = configuration
        _serverState.update {
            it.copy(
                serverType = configuration.serverType,
                serverStatus = ServerStatusEnum.NONE
            )
        }

        startAdvertising(packageName)
    }

    /**
     * Close the server and disconnect all clients.
     */
    fun closeServer() {
        EasyLocalGameLogger.i("Closing server. Connected clients: ${_serverState.value.connectedClients.size}", TAG)
        
        connectionsManager.stopAllEndpoints()
        connectionsManager.stopAdvertising()
        connectionsManager.stopDiscovery()

        _serverState.update { ServerState(serverStatus = ServerStatusEnum.CLOSED) }
        EasyLocalGameLogger.i("Server closed successfully", TAG)
    }

    /**
     * Stop advertising but keep existing connections.
     */
    fun stopAdvertising() {
        EasyLocalGameLogger.i("Stopping advertising, keeping ${_serverState.value.connectedClients.size} active connections", TAG)
        
        connectionsManager.stopAdvertising()
        _serverState.update {
            it.copy(serverStatus = ServerStatusEnum.ACTIVE)
        }
    }

    /**
     * Send a payload to a specific client.
     *
     * @param clientId The endpoint ID of the target client
     * @param payload The data to send
     */
    fun sendPayload(clientId: String, payload: ByteArray) {
        if (_serverState.value.connectedClients.contains(clientId)) {
            EasyLocalGameLogger.d("Sending payload to client $clientId (${payload.size} bytes)", TAG)
            connectionsManager.sendPayload(clientId, payload)
        } else {
            EasyLocalGameLogger.w("Cannot send payload: client $clientId not connected", TAG)
        }
    }

    /**
     * Send a payload to all connected clients.
     *
     * @param payload The data to send
     */
    fun sendPayload(payload: ByteArray) {
        val clients = _serverState.value.connectedClients
        if (clients.isNotEmpty()) {
            EasyLocalGameLogger.d("Broadcasting payload to ${clients.size} clients (${payload.size} bytes)", TAG)
            connectionsManager.sendPayload(clients, payload)
        } else {
            EasyLocalGameLogger.d("No clients connected, skipping broadcast", TAG)
        }
    }

    private fun startAdvertising(packageName: String) {
        EasyLocalGameLogger.d("Preparing to advertise as '${serverConfiguration.serverAsPlayerName}' on service '$packageName'", TAG)
        
        connectionsManager.stopAdvertising()
        connectionsManager.stopAllEndpoints()

        payloadCallbacks = createPayloadCallbacks()

        connectionsManager.startAdvertising(
            name = serverConfiguration.serverAsPlayerName,
            serviceId = packageName,
            strategy = ConnectionStrategy.P2P_STAR,
            connectionCallbacks = connectionCallbacks,
            payloadCallbacks = payloadCallbacks,
            onSuccess = {
                EasyLocalGameLogger.i("✅ Advertising started successfully as '${serverConfiguration.serverAsPlayerName}'", TAG)
                _serverState.update {
                    it.copy(serverStatus = ServerStatusEnum.ADVERTISING)
                }
            },
            onFailure = { exception ->
                EasyLocalGameLogger.e("❌ Advertising failed: ${exception.message}", TAG, exception)
                _serverState.update {
                    it.copy(serverStatus = ServerStatusEnum.ADVERTISING_FAILED)
                }
            }
        )
    }

    private fun clientConnected(endpointId: String) {
        val currentCount = _serverState.value.connectedClients.size
        val maxConnections = serverConfiguration.maximumConnections
        
        if (currentCount >= maxConnections) {
            EasyLocalGameLogger.w("Room is full ($currentCount/$maxConnections), rejecting client $endpointId", TAG)
            val payload = toServerPayload<String?>(ServerPayloadType.ROOM_IS_FULL, null)
            connectionsManager.sendPayload(endpointId, payload)
            connectionsManager.disconnectFromEndpoint(endpointId)
            return
        }

        EasyLocalGameLogger.i("✅ Client connected: $endpointId (${currentCount + 1}/$maxConnections)", TAG)
        
        _serverState.update {
            it.copy(connectedClients = it.connectedClients + endpointId)
        }

        // Notify client of successful connection
        val payload = toServerPayload(ServerPayloadType.CLIENT_CONNECTED, _serverState.value.serverType.name)
        connectionsManager.sendPayload(endpointId, payload)
    }

    private fun clientDisconnected(endpointId: String) {
        val wasConnected = _serverState.value.connectedClients.contains(endpointId)
        
        _serverState.update {
            it.copy(connectedClients = it.connectedClients.filter { id -> id != endpointId })
        }

        if (wasConnected) {
            EasyLocalGameLogger.i("👋 Client disconnected: $endpointId (${_serverState.value.connectedClients.size} remaining)", TAG)
        } else {
            EasyLocalGameLogger.d("Client $endpointId disconnected (was not in connected list)", TAG)
        }

        clientAction(ClientAction.Disconnect(endpointId))
    }

    private val connectionCallbacks = object : ConnectionCallbacks {
        override fun onConnectionInitiated(endpointId: String, endpointName: String) {
            EasyLocalGameLogger.d("Connection initiated from '$endpointName' ($endpointId)", TAG)
            
            if (_serverState.value.connectedClients.size >= serverConfiguration.maximumConnections) {
                EasyLocalGameLogger.w("Rejecting connection from $endpointId - room is full", TAG)
                connectionsManager.rejectConnection(endpointId)
            } else {
                EasyLocalGameLogger.d("Accepting connection from '$endpointName' ($endpointId)", TAG)
                connectionsManager.acceptConnection(endpointId, payloadCallbacks)
            }
        }

        override fun onConnectionResult(endpointId: String, isSuccess: Boolean, statusCode: Int) {
            if (statusCode == ConnectionStatusCodes.STATUS_OK) {
                EasyLocalGameLogger.d("Connection result OK for $endpointId", TAG)
                clientConnected(endpointId)
            } else {
                EasyLocalGameLogger.e("Connection result failed for $endpointId: statusCode=$statusCode", TAG)
                _serverState.update {
                    it.copy(serverStatus = ServerStatusEnum.ADVERTISING_FAILED)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            EasyLocalGameLogger.d("onDisconnected callback for $endpointId", TAG)
            clientDisconnected(endpointId)
        }
    }

    private fun createPayloadCallbacks() = object : PayloadCallbacks {
        override fun onPayloadReceived(endpointId: String, payload: ByteArray) {
            EasyLocalGameLogger.d("Payload received from $endpointId (${payload.size} bytes)", TAG)
            
            try {
                val (clientPayloadType, _) = fromClientPayload<Any?>(payload)
                EasyLocalGameLogger.d("Payload type: $clientPayloadType", TAG)

                when (clientPayloadType) {
                    ClientPayloadType.ESTABLISH_CONNECTION -> {
                        EasyLocalGameLogger.i("Client $endpointId establishing connection", TAG)
                        clientAction(ClientAction.EstablishConnection(endpointId, payload))
                    }
                    ClientPayloadType.ACTION_DISCONNECTED -> {
                        EasyLocalGameLogger.i("Client $endpointId sent disconnect action", TAG)
                        clientDisconnected(endpointId)
                    }
                    null -> {
                        EasyLocalGameLogger.d("Custom payload from $endpointId", TAG)
                        clientAction(ClientAction.PayloadAction(endpointId, payload))
                    }
                }
            } catch (e: Exception) {
                EasyLocalGameLogger.w("Failed to parse payload from $endpointId, treating as custom: ${e.message}", TAG)
                clientAction(ClientAction.PayloadAction(endpointId, payload))
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
                true -> EasyLocalGameLogger.d("Payload $payloadId transfer complete to $endpointId", TAG)
                false -> EasyLocalGameLogger.w("Payload $payloadId transfer failed to $endpointId", TAG)
                null -> {} // In progress, don't log to avoid spam
            }
        }
    }
}

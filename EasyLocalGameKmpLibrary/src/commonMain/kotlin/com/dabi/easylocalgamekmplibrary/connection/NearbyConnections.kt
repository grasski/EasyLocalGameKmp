package com.dabi.easylocalgamekmplibrary.connection

/**
 * Callback interface for connection lifecycle events.
 */
interface ConnectionCallbacks {
    /**
     * Called when a connection is initiated with an endpoint.
     * @param endpointId The endpoint identifier
     * @param endpointName The name of the endpoint
     */
    fun onConnectionInitiated(endpointId: String, endpointName: String)

    /**
     * Called when connection result is received.
     * @param endpointId The endpoint identifier
     * @param isSuccess Whether the connection was successful
     * @param statusCode Status code of the connection result
     */
    fun onConnectionResult(endpointId: String, isSuccess: Boolean, statusCode: Int)

    /**
     * Called when an endpoint disconnects.
     * @param endpointId The endpoint identifier
     */
    fun onDisconnected(endpointId: String)
}

/**
 * Callback interface for endpoint discovery events.
 */
interface DiscoveryCallbacks {
    /**
     * Called when an endpoint is found.
     * @param endpointId The endpoint identifier
     * @param endpointName The name of the endpoint
     * @param serviceId The service ID of the endpoint
     */
    fun onEndpointFound(endpointId: String, endpointName: String, serviceId: String)

    /**
     * Called when a previously discovered endpoint is lost.
     * @param endpointId The endpoint identifier
     */
    fun onEndpointLost(endpointId: String)
}

/**
 * Callback interface for payload events.
 */
interface PayloadCallbacks {
    /**
     * Called when a payload is received.
     * @param endpointId The endpoint that sent the payload
     * @param payload The received payload data
     */
    fun onPayloadReceived(endpointId: String, payload: ByteArray)

    /**
     * Called with transfer progress updates.
     * @param endpointId The endpoint identifier
     * @param payloadId The payload identifier
     * @param bytesTransferred Bytes transferred so far
     * @param totalBytes Total bytes to transfer
     * @param isSuccess Whether transfer completed successfully (when complete)
     */
    fun onPayloadTransferUpdate(
        endpointId: String,
        payloadId: Long,
        bytesTransferred: Long,
        totalBytes: Long,
        isSuccess: Boolean?
    )
}

/**
 * Connection strategy for Nearby Connections.
 */
enum class ConnectionStrategy {
    /** Point-to-point: 1 to 1 connection */
    P2P_POINT_TO_POINT,
    /** Star: 1 to N connections, server at center */
    P2P_STAR,
    /** Cluster: M to N connections */
    P2P_CLUSTER
}

/**
 * Status codes for connection results.
 */
object ConnectionStatusCodes {
    const val STATUS_OK = 0
    const val STATUS_CONNECTION_REJECTED = 8004
    const val STATUS_ERROR = -1
}

/**
 * Platform-specific Nearby Connections manager.
 * This is an expect declaration that must have actual implementations
 * for each platform (Android, iOS).
 */
expect class NearbyConnectionsManager {
    /**
     * Start advertising this device as a server.
     * @param name The name to advertise
     * @param serviceId The service identifier (usually package name)
     * @param strategy The connection strategy to use
     * @param connectionCallbacks Callbacks for connection events
     * @param payloadCallbacks Callbacks for payload events
     * @param onSuccess Called when advertising starts successfully
     * @param onFailure Called when advertising fails to start
     */
    fun startAdvertising(
        name: String,
        serviceId: String,
        strategy: ConnectionStrategy,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Stop advertising.
     */
    fun stopAdvertising()

    /**
     * Start discovering nearby endpoints.
     * @param serviceId The service identifier to search for
     * @param strategy The connection strategy to use
     * @param discoveryCallbacks Callbacks for discovery events
     * @param onSuccess Called when discovery starts successfully
     * @param onFailure Called when discovery fails to start
     */
    fun startDiscovery(
        serviceId: String,
        strategy: ConnectionStrategy,
        discoveryCallbacks: DiscoveryCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Stop discovering.
     */
    fun stopDiscovery()

    /**
     * Request a connection to an endpoint.
     * @param name The name to present to the other endpoint
     * @param endpointId The endpoint to connect to
     * @param connectionCallbacks Callbacks for connection events
     * @param payloadCallbacks Callbacks for payload events
     * @param onSuccess Called when request is successfully sent
     * @param onFailure Called when request fails
     */
    fun requestConnection(
        name: String,
        endpointId: String,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Accept a connection from an endpoint.
     * @param endpointId The endpoint to accept
     * @param payloadCallbacks Callbacks for payload events
     */
    fun acceptConnection(endpointId: String, payloadCallbacks: PayloadCallbacks)

    /**
     * Reject a connection from an endpoint.
     * @param endpointId The endpoint to reject
     */
    fun rejectConnection(endpointId: String)

    /**
     * Send a payload to a specific endpoint.
     * @param endpointId The endpoint to send to
     * @param payload The data to send
     */
    fun sendPayload(endpointId: String, payload: ByteArray)

    /**
     * Send a payload to multiple endpoints.
     * @param endpointIds List of endpoints to send to
     * @param payload The data to send
     */
    fun sendPayload(endpointIds: List<String>, payload: ByteArray)

    /**
     * Disconnect from a specific endpoint.
     * @param endpointId The endpoint to disconnect from
     */
    fun disconnectFromEndpoint(endpointId: String)

    /**
     * Stop all connections and endpoints.
     */
    fun stopAllEndpoints()
}

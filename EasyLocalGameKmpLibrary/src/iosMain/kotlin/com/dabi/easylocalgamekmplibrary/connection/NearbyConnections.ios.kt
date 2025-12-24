package com.dabi.easylocalgamekmplibrary.connection

import com.dabi.easylocalgamekmplibrary.logging.EasyLocalGameLogger

/**
 * iOS implementation of NearbyConnectionsManager.
 * 
 * This implementation bridges to Swift code using the NearbyConnections iOS SDK.
 * The Swift wrapper (NearbyConnectionsWrapper) must be implemented in your iOS project
 * and registered with this manager.
 * 
 * IMPORTANT: To use this on iOS, you need to:
 * 1. Add NearbyConnections SDK to your iOS project via CocoaPods or SPM
 * 2. Implement NearbyConnectionsWrapper.swift in your iosApp
 * 3. Call NearbyConnectionsManager.setSwiftBridge() during app initialization
 */
actual class NearbyConnectionsManager {
    
    private var bridge: NearbyConnectionsBridge? = null

    companion object {
        private const val TAG = "NearbyConnections"
    }

    /**
     * Set the Swift bridge implementation.
     * Call this from your iOS app initialization with the Swift wrapper instance.
     */
    fun setSwiftBridge(bridge: NearbyConnectionsBridge) {
        EasyLocalGameLogger.i("[iOS] Swift bridge set", TAG)
        this.bridge = bridge
    }

    actual fun startAdvertising(
        name: String,
        serviceId: String,
        strategy: ConnectionStrategy,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        EasyLocalGameLogger.d("[iOS] Starting advertising: name='$name', serviceId='$serviceId', strategy=$strategy", TAG)
        
        val b = bridge
        if (b == null) {
            EasyLocalGameLogger.e("[iOS] ❌ NearbyConnectionsBridge not set!", TAG)
            onFailure(IllegalStateException("NearbyConnectionsBridge not set. Call setSwiftBridge() first."))
            return
        }
        b.startAdvertising(
            name = name,
            serviceId = serviceId,
            strategy = strategy,
            connectionCallbacks = connectionCallbacks,
            payloadCallbacks = payloadCallbacks,
            onSuccess = {
                EasyLocalGameLogger.i("[iOS] ✅ Advertising started successfully", TAG)
                onSuccess()
            },
            onFailure = { e ->
                EasyLocalGameLogger.e("[iOS] ❌ Advertising failed: ${e.message}", TAG)
                onFailure(e)
            }
        )
    }

    actual fun stopAdvertising() {
        EasyLocalGameLogger.d("[iOS] Stopping advertising", TAG)
        bridge?.stopAdvertising()
    }

    actual fun startDiscovery(
        serviceId: String,
        strategy: ConnectionStrategy,
        discoveryCallbacks: DiscoveryCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        EasyLocalGameLogger.d("[iOS] Starting discovery: serviceId='$serviceId', strategy=$strategy", TAG)
        
        val b = bridge
        if (b == null) {
            EasyLocalGameLogger.e("[iOS] ❌ NearbyConnectionsBridge not set!", TAG)
            onFailure(IllegalStateException("NearbyConnectionsBridge not set. Call setSwiftBridge() first."))
            return
        }
        b.startDiscovery(
            serviceId = serviceId,
            strategy = strategy,
            discoveryCallbacks = discoveryCallbacks,
            onSuccess = {
                EasyLocalGameLogger.i("[iOS] ✅ Discovery started successfully", TAG)
                onSuccess()
            },
            onFailure = { e ->
                EasyLocalGameLogger.e("[iOS] ❌ Discovery failed: ${e.message}", TAG)
                onFailure(e)
            }
        )
    }

    actual fun stopDiscovery() {
        EasyLocalGameLogger.d("[iOS] Stopping discovery", TAG)
        bridge?.stopDiscovery()
    }

    actual fun requestConnection(
        name: String,
        endpointId: String,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        EasyLocalGameLogger.d("[iOS] Requesting connection: name='$name', endpointId='$endpointId'", TAG)
        
        val b = bridge
        if (b == null) {
            EasyLocalGameLogger.e("[iOS] ❌ NearbyConnectionsBridge not set!", TAG)
            onFailure(IllegalStateException("NearbyConnectionsBridge not set. Call setSwiftBridge() first."))
            return
        }
        b.requestConnection(
            name = name,
            endpointId = endpointId,
            connectionCallbacks = connectionCallbacks,
            payloadCallbacks = payloadCallbacks,
            onSuccess = {
                EasyLocalGameLogger.d("[iOS] Connection request sent to $endpointId", TAG)
                onSuccess()
            },
            onFailure = { e ->
                EasyLocalGameLogger.e("[iOS] ❌ Connection request failed: ${e.message}", TAG)
                onFailure(e)
            }
        )
    }

    actual fun acceptConnection(endpointId: String, payloadCallbacks: PayloadCallbacks) {
        EasyLocalGameLogger.d("[iOS] Accepting connection from $endpointId", TAG)
        bridge?.acceptConnection(endpointId, payloadCallbacks)
    }

    actual fun rejectConnection(endpointId: String) {
        EasyLocalGameLogger.d("[iOS] Rejecting connection from $endpointId", TAG)
        bridge?.rejectConnection(endpointId)
    }

    actual fun sendPayload(endpointId: String, payload: ByteArray) {
        EasyLocalGameLogger.d("[iOS] Sending payload to $endpointId (${payload.size} bytes)", TAG)
        bridge?.sendPayload(endpointId, payload)
    }

    actual fun sendPayload(endpointIds: List<String>, payload: ByteArray) {
        EasyLocalGameLogger.d("[iOS] Sending payload to ${endpointIds.size} endpoints (${payload.size} bytes)", TAG)
        bridge?.sendPayload(endpointIds, payload)
    }

    actual fun disconnectFromEndpoint(endpointId: String) {
        EasyLocalGameLogger.d("[iOS] Disconnecting from endpoint: $endpointId", TAG)
        bridge?.disconnectFromEndpoint(endpointId)
    }

    actual fun stopAllEndpoints() {
        EasyLocalGameLogger.d("[iOS] Stopping all endpoints", TAG)
        bridge?.stopAllEndpoints()
    }
}

/**
 * Interface that the Swift wrapper must implement.
 * Create a Swift class that conforms to this interface and bridges to the NearbyConnections iOS SDK.
 */
interface NearbyConnectionsBridge {
    fun startAdvertising(
        name: String,
        serviceId: String,
        strategy: ConnectionStrategy,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun stopAdvertising()

    fun startDiscovery(
        serviceId: String,
        strategy: ConnectionStrategy,
        discoveryCallbacks: DiscoveryCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun stopDiscovery()

    fun requestConnection(
        name: String,
        endpointId: String,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun acceptConnection(endpointId: String, payloadCallbacks: PayloadCallbacks)

    fun rejectConnection(endpointId: String)

    fun sendPayload(endpointId: String, payload: ByteArray)

    fun sendPayload(endpointIds: List<String>, payload: ByteArray)

    fun disconnectFromEndpoint(endpointId: String)

    fun stopAllEndpoints()
}

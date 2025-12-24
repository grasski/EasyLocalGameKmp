package com.dabi.easylocalgamekmplibrary.connection

import com.dabi.easylocalgamekmplibrary.logging.EasyLocalGameLogger
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes as GmsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy

/**
 * Android implementation of NearbyConnectionsManager using Google Play Services.
 */
actual class NearbyConnectionsManager(
    private val connectionsClient: ConnectionsClient
) {
    private var currentPayloadCallbacks: PayloadCallbacks? = null

    companion object {
        private const val TAG = "NearbyConnections"
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
        EasyLocalGameLogger.d("[Android] Starting advertising: name='$name', serviceId='$serviceId', strategy=$strategy", TAG)
        
        currentPayloadCallbacks = payloadCallbacks

        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(strategy.toGmsStrategy())
            .build()

        val lifecycleCallback = createConnectionLifecycleCallback(connectionCallbacks, payloadCallbacks)

        connectionsClient.startAdvertising(name, serviceId, lifecycleCallback, advertisingOptions)
            .addOnSuccessListener { 
                EasyLocalGameLogger.i("[Android] ✅ Advertising started successfully", TAG)
                onSuccess() 
            }
            .addOnFailureListener { 
                EasyLocalGameLogger.e("[Android] ❌ Advertising failed: ${it.message}", TAG, it)
                onFailure(it) 
            }
    }

    actual fun stopAdvertising() {
        EasyLocalGameLogger.d("[Android] Stopping advertising", TAG)
        connectionsClient.stopAdvertising()
    }

    actual fun startDiscovery(
        serviceId: String,
        strategy: ConnectionStrategy,
        discoveryCallbacks: DiscoveryCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        EasyLocalGameLogger.d("[Android] Starting discovery: serviceId='$serviceId', strategy=$strategy", TAG)
        
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(strategy.toGmsStrategy())
            .build()

        val endpointCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                EasyLocalGameLogger.i("[Android] Endpoint found: '${info.endpointName}' ($endpointId)", TAG)
                discoveryCallbacks.onEndpointFound(endpointId, info.endpointName, info.serviceId)
            }

            override fun onEndpointLost(endpointId: String) {
                EasyLocalGameLogger.w("[Android] Endpoint lost: $endpointId", TAG)
                discoveryCallbacks.onEndpointLost(endpointId)
            }
        }

        connectionsClient.startDiscovery(serviceId, endpointCallback, discoveryOptions)
            .addOnSuccessListener { 
                EasyLocalGameLogger.i("[Android] ✅ Discovery started successfully", TAG)
                onSuccess() 
            }
            .addOnFailureListener { 
                EasyLocalGameLogger.e("[Android] ❌ Discovery failed: ${it.message}", TAG, it)
                onFailure(it) 
            }
    }

    actual fun stopDiscovery() {
        EasyLocalGameLogger.d("[Android] Stopping discovery", TAG)
        connectionsClient.stopDiscovery()
    }

    actual fun requestConnection(
        name: String,
        endpointId: String,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        EasyLocalGameLogger.d("[Android] Requesting connection: name='$name', endpointId='$endpointId'", TAG)
        
        currentPayloadCallbacks = payloadCallbacks

        val lifecycleCallback = createConnectionLifecycleCallback(connectionCallbacks, payloadCallbacks)

        connectionsClient.requestConnection(name, endpointId, lifecycleCallback)
            .addOnSuccessListener { 
                EasyLocalGameLogger.d("[Android] Connection request sent to $endpointId", TAG)
                onSuccess() 
            }
            .addOnFailureListener { 
                EasyLocalGameLogger.e("[Android] Connection request failed: ${it.message}", TAG, it)
                onFailure(it) 
            }
    }

    actual fun acceptConnection(endpointId: String, payloadCallbacks: PayloadCallbacks) {
        EasyLocalGameLogger.d("[Android] Accepting connection from $endpointId", TAG)
        val callback = createPayloadCallback(payloadCallbacks)
        connectionsClient.acceptConnection(endpointId, callback)
    }

    actual fun rejectConnection(endpointId: String) {
        EasyLocalGameLogger.d("[Android] Rejecting connection from $endpointId", TAG)
        connectionsClient.rejectConnection(endpointId)
    }

    actual fun sendPayload(endpointId: String, payload: ByteArray) {
        EasyLocalGameLogger.d("[Android] Sending payload to $endpointId (${payload.size} bytes)", TAG)
        val gmsPayload = Payload.fromBytes(payload)
        connectionsClient.sendPayload(endpointId, gmsPayload)
    }

    actual fun sendPayload(endpointIds: List<String>, payload: ByteArray) {
        if (endpointIds.isNotEmpty()) {
            EasyLocalGameLogger.d("[Android] Sending payload to ${endpointIds.size} endpoints (${payload.size} bytes)", TAG)
            val gmsPayload = Payload.fromBytes(payload)
            connectionsClient.sendPayload(endpointIds, gmsPayload)
        }
    }

    actual fun disconnectFromEndpoint(endpointId: String) {
        EasyLocalGameLogger.d("[Android] Disconnecting from endpoint: $endpointId", TAG)
        connectionsClient.disconnectFromEndpoint(endpointId)
    }

    actual fun stopAllEndpoints() {
        EasyLocalGameLogger.d("[Android] Stopping all endpoints", TAG)
        connectionsClient.stopAllEndpoints()
    }

    private fun createConnectionLifecycleCallback(
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks
    ) = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            EasyLocalGameLogger.d("[Android] Connection initiated: ${info.endpointName} ($endpointId)", TAG)
            connectionCallbacks.onConnectionInitiated(endpointId, info.endpointName)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            val isSuccess = resolution.status.statusCode == GmsStatusCodes.STATUS_OK
            val statusCode = when (resolution.status.statusCode) {
                GmsStatusCodes.STATUS_OK -> ConnectionStatusCodes.STATUS_OK
                GmsStatusCodes.STATUS_CONNECTION_REJECTED -> ConnectionStatusCodes.STATUS_CONNECTION_REJECTED
                else -> ConnectionStatusCodes.STATUS_ERROR
            }
            
            if (isSuccess) {
                EasyLocalGameLogger.i("[Android] ✅ Connection successful: $endpointId", TAG)
            } else {
                EasyLocalGameLogger.w("[Android] Connection result: statusCode=${resolution.status.statusCode}, message=${resolution.status.statusMessage}", TAG)
            }
            
            connectionCallbacks.onConnectionResult(endpointId, isSuccess, statusCode)
        }

        override fun onDisconnected(endpointId: String) {
            EasyLocalGameLogger.w("[Android] Disconnected: $endpointId", TAG)
            connectionCallbacks.onDisconnected(endpointId)
        }
    }

    private fun createPayloadCallback(payloadCallbacks: PayloadCallbacks) = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                EasyLocalGameLogger.d("[Android] Payload received from $endpointId (${bytes.size} bytes)", TAG)
                payloadCallbacks.onPayloadReceived(endpointId, bytes)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            val isSuccess = when (update.status) {
                PayloadTransferUpdate.Status.SUCCESS -> true
                PayloadTransferUpdate.Status.FAILURE -> false
                PayloadTransferUpdate.Status.CANCELED -> false
                else -> null // In progress
            }
            
            if (isSuccess != null) {
                if (isSuccess) {
                    EasyLocalGameLogger.d("[Android] Payload transfer complete: ${update.payloadId}", TAG)
                } else {
                    EasyLocalGameLogger.w("[Android] Payload transfer failed: ${update.payloadId}", TAG)
                }
            }
            
            payloadCallbacks.onPayloadTransferUpdate(
                endpointId,
                update.payloadId,
                update.bytesTransferred,
                update.totalBytes,
                isSuccess
            )
        }
    }

    private fun ConnectionStrategy.toGmsStrategy(): Strategy = when (this) {
        ConnectionStrategy.P2P_POINT_TO_POINT -> Strategy.P2P_POINT_TO_POINT
        ConnectionStrategy.P2P_STAR -> Strategy.P2P_STAR
        ConnectionStrategy.P2P_CLUSTER -> Strategy.P2P_CLUSTER
    }
}

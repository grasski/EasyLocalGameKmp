package com.dabi.easylocalgamekmplibrary.client

import com.dabi.easylocalgamekmplibrary.server.ServerType
import kotlinx.serialization.Serializable

/**
 * Current state of the client connection.
 *
 * @param connectionStatus Current connection status
 * @param serverID Endpoint ID of the connected server (empty if not connected)
 * @param serverType Type of server connected to
 */
@Serializable
data class ClientState(
    val connectionStatus: ConnectionStatusEnum = ConnectionStatusEnum.NONE,
    val serverID: String = "",
    val serverType: ServerType = ServerType.IS_TABLE
)

/**
 * Possible states of the client connection.
 */
@Serializable
enum class ConnectionStatusEnum {
    /** No connection has been initialized. */
    NONE,
    /** The client is searching for servers. */
    CONNECTING,
    /** Connection attempt failed. */
    CONNECTING_FAILED,
    /** Connection was rejected by the server. */
    CONNECTING_REJECTED,
    /** Lost connection to the server endpoint. */
    ENDPOINT_LOST,
    /** Room is full, cannot join. */
    ROOM_IS_FULL,
    /** Successfully connected to server. */
    CONNECTED,
    /** Connection fully established after exchanging player info. */
    CONNECTION_ESTABLISHED,
    /** Disconnected from server. */
    DISCONNECTED
}

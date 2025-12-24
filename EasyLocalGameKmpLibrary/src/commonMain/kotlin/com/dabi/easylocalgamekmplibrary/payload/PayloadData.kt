package com.dabi.easylocalgamekmplibrary.payload

import kotlinx.serialization.Serializable

/**
 * Payload types sent from client to server.
 */
@Serializable
enum class ClientPayloadType {
    /** Client sends connection info after initial connection. */
    ESTABLISH_CONNECTION,
    /** Client notifies server of intentional disconnect. */
    ACTION_DISCONNECTED,
}

/**
 * Payload types sent from server to client.
 */
@Serializable
enum class ServerPayloadType {
    /** Server confirms client connection and sends server type. */
    CLIENT_CONNECTED,
    /** Server rejects connection because room is full. */
    ROOM_IS_FULL,
    /** Server sends updated player state to specific client. */
    UPDATE_PLAYER_STATE,
    /** Server broadcasts updated game state to all clients. */
    UPDATE_GAME_STATE,
}

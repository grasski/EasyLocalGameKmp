package com.dabi.easylocalgamekmplibrary.server

import kotlinx.serialization.Serializable

/**
 * Configuration for starting a server.
 *
 * @param serverType Whether the server host is also a player or just a table/spectator
 * @param maximumConnections Maximum number of clients that can connect
 * @param serverAsPlayerName Name shown to clients when server is advertising
 */
@Serializable
data class ServerConfiguration(
    val serverType: ServerType,
    val maximumConnections: Int,
    val serverAsPlayerName: String = "server",
)

/**
 * Type of server - whether the host device is a player or just a table.
 */
@Serializable
enum class ServerType {
    /** Server host is also a player in the game. */
    IS_PLAYER,
    /** Server is just a "table" - hosts the game but doesn't play. */
    IS_TABLE
}

/**
 * Current state of the server.
 *
 * @param serverStatus Current status of the server
 * @param serverType Type of server (player or table)
 * @param connectedClients List of endpoint IDs of connected clients
 */
@Serializable
data class ServerState(
    val serverStatus: ServerStatusEnum = ServerStatusEnum.NONE,
    val serverType: ServerType = ServerType.IS_TABLE,
    val connectedClients: List<String> = emptyList()
)

/**
 * Possible states of the server.
 */
@Serializable
enum class ServerStatusEnum {
    /** No advertising has been initialized. */
    NONE,
    /** The advertising failed to start. */
    ADVERTISING_FAILED,
    /** The server is advertising and accepting connections. */
    ADVERTISING,
    /** The server is active but no longer accepting new connections. */
    ACTIVE,
    /** The server was closed. */
    CLOSED
}

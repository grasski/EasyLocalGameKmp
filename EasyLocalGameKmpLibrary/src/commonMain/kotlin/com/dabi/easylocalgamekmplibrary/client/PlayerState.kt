package com.dabi.easylocalgamekmplibrary.client

import kotlinx.serialization.Serializable

/**
 * Interface for player connection state sent to server when establishing connection.
 */
interface IPlayerConnectionState {
    val nickname: String
    val avatarId: Int?
}

/**
 * Default implementation of player connection state.
 */
@Serializable
data class PlayerConnectionState(
    override val nickname: String,
    override val avatarId: Int? = null
) : IPlayerConnectionState

/**
 * Interface for player state maintained during the game.
 */
interface IPlayerState {
    val nickname: String
    val id: String
    val isServer: Boolean
    val avatarId: Int?
}

/**
 * Default implementation of player state.
 */
@Serializable
data class PlayerState(
    override val nickname: String,
    override val id: String,
    override val isServer: Boolean = false,
    override val avatarId: Int? = null
) : IPlayerState

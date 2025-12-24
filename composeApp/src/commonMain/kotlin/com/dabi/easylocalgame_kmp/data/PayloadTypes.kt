package com.dabi.easylocalgame_kmp.data

import kotlinx.serialization.Serializable

/**
 * Demo app data models.
 * 
 * This file shows how to:
 * 1. Define custom client payload types for game-specific actions
 * 2. Create your own game state classes
 */

// =============================================================================
// CUSTOM CLIENT PAYLOAD TYPES
// =============================================================================
// Only define types for YOUR game-specific actions.
// The library already provides: ESTABLISH_CONNECTION, ACTION_DISCONNECTED
// Use PayloadAction for custom types parsed via fromClientPayload

/**
 * Custom client actions for the demo game.
 * 
 * Example: For a poker game, you might have:
 * ACTION_READY, ACTION_CHECK, ACTION_CALL, ACTION_RAISE, ACTION_FOLD
 */
@Serializable
enum class DemoClientAction {
    /** Player is ready to start */
    READY,
    /** Player sends a chat message */
    SEND_MESSAGE,
    /** Player requests current state */
    REQUEST_STATE,
}

// =============================================================================
// GAME STATE - Shared with all clients
// =============================================================================

/**
 * Your game state that gets sent to clients.
 * 
 * Use library's ServerPayloadType.UPDATE_GAME_STATE to send this.
 */
@Serializable
data class DemoGameState(
    val messageCount: Int = 0,
    val lastMessage: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val players: List<DemoPlayerInfo> = emptyList(),
    val isGameStarted: Boolean = false
)

/**
 * Player info for display.
 */
@Serializable
data class DemoPlayerInfo(
    val id: String,
    val name: String,
    val isReady: Boolean = false
)

// =============================================================================
// MESSAGE DATA
// =============================================================================

/**
 * Chat message data.
 */
@Serializable
data class ChatMessage(
    val senderName: String,
    val message: String,
    val timestamp: Long = currentTimeMillis()
)

// Helper function for current time
expect fun currentTimeMillis(): Long

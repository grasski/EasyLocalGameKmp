package com.dabi.easylocalgamekmplibrary.actions

/**
 * Actions sent from client to server.
 * Used by the server to handle client events.
 */
sealed class ClientAction {
    /**
     * Client is establishing connection and sending their player info.
     * @param endpointID The endpoint ID of the connecting client
     * @param payload Raw payload bytes containing player connection info
     */
    data class EstablishConnection(
        val endpointID: String,
        val payload: ByteArray
    ) : ClientAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as EstablishConnection
            return endpointID == other.endpointID && payload.contentEquals(other.payload)
        }
        override fun hashCode(): Int {
            var result = endpointID.hashCode()
            result = 31 * result + payload.contentHashCode()
            return result
        }
    }

    /**
     * Client has disconnected.
     * @param endpointID The endpoint ID of the disconnected client
     */
    data class Disconnect(val endpointID: String) : ClientAction()

    /**
     * Client sent a custom payload action.
     * @param endpointID The endpoint ID of the client
     * @param payload Raw payload bytes containing custom action data
     */
    data class PayloadAction(
        val endpointID: String,
        val payload: ByteArray
    ) : ClientAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as PayloadAction
            return endpointID == other.endpointID && payload.contentEquals(other.payload)
        }
        override fun hashCode(): Int {
            var result = endpointID.hashCode()
            result = 31 * result + payload.contentHashCode()
            return result
        }
    }
}

/**
 * Actions sent from server to client.
 * Used by the client to handle server events.
 */
sealed class ServerAction {
    /**
     * Server sent updated game state.
     * @param payload Raw payload bytes containing game state
     */
    data class UpdateGameState(val payload: ByteArray) : ServerAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return payload.contentEquals((other as UpdateGameState).payload)
        }
        override fun hashCode(): Int = payload.contentHashCode()
    }

    /**
     * Server sent updated player state.
     * @param payload Raw payload bytes containing player state
     */
    data class UpdatePlayerState(val payload: ByteArray) : ServerAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return payload.contentEquals((other as UpdatePlayerState).payload)
        }
        override fun hashCode(): Int = payload.contentHashCode()
    }

    /**
     * Server sent a custom payload action.
     * @param payload Raw payload bytes containing custom action data
     */
    data class PayloadAction(val payload: ByteArray) : ServerAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return payload.contentEquals((other as PayloadAction).payload)
        }
        override fun hashCode(): Int = payload.contentHashCode()
    }
}

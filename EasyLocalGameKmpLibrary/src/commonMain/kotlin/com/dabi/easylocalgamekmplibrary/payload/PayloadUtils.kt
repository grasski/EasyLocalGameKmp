package com.dabi.easylocalgamekmplibrary.payload

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * JSON configuration for payload serialization.
 * Configured to be lenient and handle unknown keys gracefully.
 */
val payloadJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    prettyPrint = false
}

/**
 * Wrapper class for payload data to include type information.
 */
@kotlinx.serialization.Serializable
data class PayloadWrapper<T>(
    val type: String,
    val data: T?
)

// =============================================================================
// GENERIC PAYLOAD FUNCTIONS - For custom enum types
// =============================================================================

/**
 * Converts a payload type and data to a ByteArray for transmission.
 *
 * This is the main function for creating payloads. Works with any type string,
 * including custom enum types:
 *
 * ```kotlin
 * // With custom enum
 * enum class MyPayloadType { ACTION_READY, ACTION_CALL }
 * val payload = toPayload(MyPayloadType.ACTION_READY.name, myData)
 *
 * // Or with library types
 * val payload = toPayload(ServerPayloadType.UPDATE_GAME_STATE.name, gameState)
 * ```
 *
 * @param payloadType The type identifier (usually enum.name)
 * @param data The data to serialize (must be @Serializable)
 * @return ByteArray ready for Nearby Connections transmission
 */
inline fun <reified T> toPayload(payloadType: String, data: T?): ByteArray {
    val wrapper = PayloadWrapper(type = payloadType, data = data)
    val json = payloadJson.encodeToString(PayloadWrapper.serializer(serializer<T>()), wrapper)
    return json.encodeToByteArray()
}

/**
 * Converts a ByteArray back to a Pair of type string and data.
 *
 * Returns the raw type string, so you can parse it to any enum type:
 *
 * ```kotlin
 * // Get raw type string
 * val (typeStr, data) = fromPayload<MyData>(payload)
 * val myType = MyPayloadType.valueOf(typeStr)  // Parse to your enum
 *
 * // Or use directly with when expression
 * when (typeStr) {
 *     MyPayloadType.ACTION_READY.name -> handleReady(data)
 *     MyPayloadType.ACTION_CALL.name -> handleCall(data)
 * }
 * ```
 *
 * @param payload The ByteArray received from Nearby Connections
 * @return Pair of type string and deserialized data
 */
inline fun <reified T> fromPayload(payload: ByteArray): Pair<String, T?> {
    val json = payload.decodeToString()
    val wrapper = payloadJson.decodeFromString(PayloadWrapper.serializer(serializer<T>()), json)
    return Pair(wrapper.type, wrapper.data)
}

/**
 * You can use this function to convert Any type from fromPayload result
 * to your specified type using JSON re-serialization.
 *
 * Useful when you first parse with Any? and then need to convert to specific type:
 *
 * ```kotlin
 * val (typeStr, rawData) = fromPayload<Any?>(payload)
 * when (typeStr) {
 *     "CHAT_MESSAGE" -> {
 *         val message = rawData.convertToType<ChatMessage>()
 *     }
 * }
 * ```
 */
inline fun <reified T> Any?.convertToType(): T? {
    if (this == null) return null
    val json = payloadJson.encodeToString(serializer<Any?>(), this)
    return payloadJson.decodeFromString(serializer<T>(), json)
}

// =============================================================================
// SERVER PAYLOAD FUNCTIONS - Convenience wrappers for library types
// =============================================================================

/**
 * Converts a ServerPayloadType and data to a ByteArray.
 *
 * Convenience wrapper for [toPayload] with [ServerPayloadType].
 */
inline fun <reified T> toServerPayload(payloadType: ServerPayloadType, data: T?): ByteArray {
    return toPayload(payloadType.name, data)
}

/**
 * Parses a payload and attempts to convert type to ServerPayloadType.
 *
 * If the type doesn't match any ServerPayloadType, returns null for the type
 * but still returns the data. Use [fromPayload] if you need the raw type string
 * for custom payload types.
 *
 * ```kotlin
 * val (serverType, data) = fromServerPayload<GameState>(payload)
 * when (serverType) {
 *     ServerPayloadType.UPDATE_GAME_STATE -> handleGameState(data)
 *     ServerPayloadType.CLIENT_CONNECTED -> handleConnected()
 *     null -> {
 *         // Unknown type - might be a custom payload, use fromPayload
 *         val (typeStr, _) = fromPayload<GameState>(payload)
 *         handleCustom(typeStr, data)
 *     }
 * }
 * ```
 */
inline fun <reified T> fromServerPayload(payload: ByteArray): Pair<ServerPayloadType?, T?> {
    val (typeStr, data) = fromPayload<T>(payload)
    val type = try {
        ServerPayloadType.valueOf(typeStr)
    } catch (e: IllegalArgumentException) {
        null
    }
    return Pair(type, data)
}

// =============================================================================
// CLIENT PAYLOAD FUNCTIONS - Convenience wrappers for library types
// =============================================================================

/**
 * Converts a ClientPayloadType and data to a ByteArray.
 *
 * Convenience wrapper for [toPayload] with [ClientPayloadType].
 */
inline fun <reified T> toClientPayload(payloadType: ClientPayloadType, data: T?): ByteArray {
    return toPayload(payloadType.name, data)
}

/**
 * Parses a payload and attempts to convert type to ClientPayloadType.
 *
 * If the type doesn't match any ClientPayloadType, returns null for the type
 * but still returns the data. Use [fromPayload] if you need the raw type string
 * for custom payload types.
 *
 * ```kotlin
 * val (clientType, data) = fromClientPayload<PlayerInfo>(payload)
 * when (clientType) {
 *     ClientPayloadType.ESTABLISH_CONNECTION -> handleConnection(data)
 *     ClientPayloadType.ACTION_DISCONNECTED -> handleDisconnect()
 *     null -> {
 *         // Unknown type - might be a custom payload, use fromPayload
 *         val (typeStr, _) = fromPayload<PlayerInfo>(payload)
 *         handleCustomClientAction(typeStr, data)
 *     }
 * }
 * ```
 */
inline fun <reified T> fromClientPayload(payload: ByteArray): Pair<ClientPayloadType?, T?> {
    val (typeStr, data) = fromPayload<T>(payload)
    val type = try {
        ClientPayloadType.valueOf(typeStr)
    } catch (e: IllegalArgumentException) {
        null
    }
    return Pair(type, data)
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

/**
 * Helper to get just the type string from a payload without deserializing the full data.
 *
 * Useful when you need to check the type before deciding how to deserialize:
 *
 * ```kotlin
 * val typeStr = getPayloadType(payload)
 * when (typeStr) {
 *     "CHAT_MESSAGE" -> {
 *         val (_, message) = fromPayload<ChatMessage>(payload)
 *     }
 *     "GAME_UPDATE" -> {
 *         val (_, state) = fromPayload<GameState>(payload)
 *     }
 * }
 * ```
 */
fun getPayloadType(payload: ByteArray): String {
    val json = payload.decodeToString()
    // Quick extraction using simple parsing
    val typeMatch = Regex(""""type"\s*:\s*"([^"]+)"""").find(json)
    return typeMatch?.groupValues?.get(1) ?: ""
}

/**
 * Check if a payload type matches a specific enum value.
 */
inline fun <reified E : Enum<E>> ByteArray.isPayloadType(enumValue: E): Boolean {
    return getPayloadType(this) == enumValue.name
}
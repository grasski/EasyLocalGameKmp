package com.dabi.easylocalgamekmplibrary.payload

import kotlinx.serialization.KSerializer
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

/**
 * Converts a payload type and data to a ByteArray for transmission.
 *
 * @param payloadType The type identifier (usually enum name)
 * @param data The data to serialize (must be @Serializable)
 * @return ByteArray ready for Nearby Connections transmission
 */
inline fun <reified T> toPayload(payloadType: String, data: T?): ByteArray {
    val wrapper = PayloadWrapper(type = payloadType, data = data)
    val json = payloadJson.encodeToString(PayloadWrapper.serializer(serializer<T>()), wrapper)
    return json.encodeToByteArray()
}

/**
 * Converts a ByteArray back to a Pair of type and data.
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
 * Converts a ServerPayloadType and data to a ByteArray.
 */
inline fun <reified T> toServerPayload(payloadType: ServerPayloadType, data: T?): ByteArray {
    return toPayload(payloadType.name, data)
}

/**
 * Converts a ClientPayloadType and data to a ByteArray.
 */
inline fun <reified T> toClientPayload(payloadType: ClientPayloadType, data: T?): ByteArray {
    return toPayload(payloadType.name, data)
}

/**
 * Parses a payload and attempts to convert type to ServerPayloadType.
 * Returns null for type if it doesn't match any ServerPayloadType.
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

/**
 * Parses a payload and attempts to convert type to ClientPayloadType.
 * Returns null for type if it doesn't match any ClientPayloadType.
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

/**
 * Helper to get just the type string from a payload without deserializing the full data.
 */
fun getPayloadType(payload: ByteArray): String {
    val json = payload.decodeToString()
    // Quick extraction of type field
    val wrapper = payloadJson.decodeFromString(PayloadWrapper.serializer(kotlinx.serialization.serializer<Any?>()), json)
    return wrapper.type
}

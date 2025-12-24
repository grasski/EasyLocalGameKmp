package com.dabi.easylocalgamekmplibrary.logging

/**
 * Log level for filtering messages.
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

/**
 * Cross-platform logger for EasyLocalGame library.
 * 
 * Configure logging behavior by setting [EasyLocalGameLogger.isEnabled] and [EasyLocalGameLogger.minLevel].
 * 
 * Example:
 * ```kotlin
 * // Enable debug logging
 * EasyLocalGameLogger.isEnabled = true
 * EasyLocalGameLogger.minLevel = LogLevel.DEBUG
 * 
 * // Disable logging in production
 * EasyLocalGameLogger.isEnabled = false
 * ```
 */
object EasyLocalGameLogger {
    
    /** Tag used for all log messages */
    const val TAG = "EasyLocalGame"
    
    /** Whether logging is enabled. Default is true. */
    var isEnabled: Boolean = true
    
    /** Minimum log level to display. Default is INFO. */
    var minLevel: LogLevel = LogLevel.INFO
    
    /** Custom log handler for integrating with external logging frameworks */
    var customHandler: ((LogLevel, String, String, Throwable?) -> Unit)? = null
    
    fun d(message: String, tag: String = TAG) {
        log(LogLevel.DEBUG, tag, message)
    }
    
    fun i(message: String, tag: String = TAG) {
        log(LogLevel.INFO, tag, message)
    }
    
    fun w(message: String, tag: String = TAG, throwable: Throwable? = null) {
        log(LogLevel.WARNING, tag, message, throwable)
    }
    
    fun e(message: String, tag: String = TAG, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }
    
    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        if (!isEnabled || level.ordinal < minLevel.ordinal) return
        
        // Use custom handler if provided
        customHandler?.let {
            it(level, tag, message, throwable)
            return
        }
        
        // Default: use platform-specific logging
        platformLog(level, tag, message, throwable)
    }
}

/**
 * Platform-specific logging implementation.
 */
internal expect fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?)
